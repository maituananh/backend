package com.spring.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.domain.enums.OrderStatus;
import com.spring.backend.domain.enums.PaymentMethod;
import com.spring.backend.domain.enums.PaymentStatus;
import com.spring.backend.domain.enums.UserRole;
import com.spring.backend.domain.order.OrderRepository;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.exception.DuplicateWebhookEventException;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.infrastructure.entity.CartItemEntity;
import com.spring.backend.infrastructure.entity.OrderEntity;
import com.spring.backend.infrastructure.entity.OrderItemEntity;
import com.spring.backend.infrastructure.entity.PaymentEntity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import com.spring.backend.infrastructure.entity.UserEntity;
import com.spring.backend.infrastructure.repository.CartItemJpaRepository;
import com.spring.backend.infrastructure.repository.OrderItemJpaRepository;
import com.spring.backend.infrastructure.repository.OrderJpaRepository;
import com.spring.backend.infrastructure.repository.PaymentJpaRepository;
import com.spring.backend.infrastructure.repository.UserJpaRepository;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository; // domain port — D-06
  private final OrderJpaRepository
      orderJpaRepository; // ALL 16 call sites route here (OrderEntity required)
  private final OrderItemJpaRepository orderItemRepository;
  private final PaymentJpaRepository paymentRepository;
  private final CartItemJpaRepository cartItemRepository;
  private final PaymentGatewayService paymentGatewayService;
  private final InventoryService inventoryService;
  private final UserHelper userHelper;
  private final UserJpaRepository userRepository;
  private final S3Adapter s3Adapter;
  private final ObjectMapper objectMapper;
  private final StripeAdapter stripeAdapter;

  // ============================================================
  // 1. CHECKOUT - Tạo order từ các cart item được chọn
  // ============================================================
  public CheckoutResponse checkout(CheckoutRequest request) {
    Long userId = userHelper.getCurrentUserId();
    UserEntity userEntity =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    // Validate cart items - truy vấn qua cart.customer_id
    List<CartItemEntity> cartItems =
        cartItemRepository.findByIdInAndCartCustomerId(request.getCartItemIds(), userId);

    if (cartItems.isEmpty()) {
      throw new RuntimeException("No items selected");
    }
    if (cartItems.size() != request.getCartItemIds().size()) {
      throw new RuntimeException("Some items are invalid or not yours");
    }

    // Kiểm tra và Giữ chỗ tồn kho (Reserve) với Pessimistic Lock
    inventoryService.reserveStock(cartItems);

    // Tính tổng tiền
    BigDecimal total =
        cartItems.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Tạo Order
    OrderEntity order =
        OrderEntity.builder()
            .user(userEntity)
            .status(OrderStatus.PENDING)
            .totalAmount(total)
            .note(request.getNote())
            .shippingName(request.getShippingName())
            .shippingPhone(request.getShippingPhone())
            .shippingAddress(request.getShippingAddress())
            .build();
    orderJpaRepository.save(order);

    // Tạo Order Items (snapshot - lưu lại thông tin tại thời điểm đặt hàng)
    List<OrderItemEntity> orderItems =
        cartItems.stream()
            .map(
                cart -> {
                  ProductEntity product = cart.getProduct();
                  // Lấy ảnh đầu tiên của sản phẩm
                  String productImage = null;
                  if (product.getImages() != null && !product.getImages().isEmpty()) {
                    productImage = product.getImages().getFirst().getFileName();
                  }
                  return (OrderItemEntity)
                      OrderItemEntity.builder()
                          .order(order)
                          .product(product)
                          .productName(product.getName())
                          .productImage(productImage)
                          .unitPrice(cart.getPrice())
                          .quantity(cart.getQuantity())
                          .subtotal(
                              cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                          .build();
                })
            .toList();
    orderItemRepository.saveAll(orderItems);

    // Tạo Payment record
    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .amount(total)
            .paymentMethod(request.getPaymentMethod())
            .status(PaymentStatus.PENDING)
            .build();
    paymentRepository.save(payment);

    // Lấy payment URL từ Gateway (Stripe session URL hoặc null nếu CASH)
    // Lưu ý: order.getItems() chưa có data trong session này nên truyền orderItems trực tiếp
    // PaymentGatewayService sẽ dùng tên từ items để tạo session description
    payment.getOrder().getItems().addAll(orderItems); // để service build tên

    String paymentUrl = paymentGatewayService.createPaymentUrl(order, payment);

    return CheckoutResponse.builder()
        .orderId(order.getId())
        .paymentUrl(paymentUrl)
        .totalAmount(total)
        .status(OrderStatus.PENDING)
        .build();
  }

  // ============================================================
  // 2. WEBHOOK - Gateway callback sau khi thanh toán
  // ============================================================
  public void handleWebhook(Event event, String rawPayload) {
    String stripeEventId = event.getId();
    String eventType = event.getType();

    // Application-layer dedup (D-02): fast path for already-processed events
    paymentRepository
        .findByStripeEventId(stripeEventId)
        .ifPresent(
            existing -> {
              log.warn(
                  "Duplicate Stripe event {} for order {} — skipping",
                  stripeEventId,
                  existing.getOrder().getId());
              throw new DuplicateWebhookEventException(stripeEventId);
            });

    // Parse the session ID from the raw payload (transactionId = Stripe session ID)
    WebhookPayload webhookPayload;
    try {
      webhookPayload = objectMapper.readValue(rawPayload, WebhookPayload.class);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Failed to parse webhook payload", e);
    }

    if (!paymentGatewayService.verifyTransaction(webhookPayload)) {
      throw new RuntimeException("Webhook payload missing transactionId");
    }

    PaymentEntity payment =
        paymentRepository
            .findByTransactionId(webhookPayload.getTransactionId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Payment not found: " + webhookPayload.getTransactionId()));

    OrderEntity order = payment.getOrder();

    // Idempotency guard: order already processed (status-level, secondary guard)
    if (order.getStatus() != OrderStatus.PENDING) {
      log.warn(
          "Order {} already processed (status={}), skipping", order.getId(), order.getStatus());
      return;
    }

    // Store raw response and event ID for dedup (D-01, D-03)
    payment.setGatewayResponse(rawPayload);
    payment.setStripeEventId(stripeEventId);

    log.info("Processing webhook event: {} for order: {}", eventType, order.getId());

    switch (eventType) {
      case "checkout.session.completed" -> handlePaymentSuccess(order, payment);
      case "checkout.session.expired" -> handlePaymentExpired(order, payment);
      case "payment_intent.payment_failed" -> handlePaymentFailed(order, payment);
      default -> {
        log.warn("Unhandled event type: {} for order: {}", eventType, order.getId());
        return; // Do not save for unknown event types
      }
    }

    orderJpaRepository.save(order);
    paymentRepository.save(payment);
  }

  private void handlePaymentExpired(OrderEntity order, PaymentEntity payment) {
    log.warn("Payment expired for order {}", order.getId());

    order.setStatus(OrderStatus.CANCELLED);
    payment.setStatus(PaymentStatus.FAILED);

    // Hoàn reserve stock
    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
    inventoryService.releaseStock(items);
  }

  private void handlePaymentSuccess(OrderEntity order, PaymentEntity payment) {
    log.info("Payment success for order {}", order.getId());

    order.setStatus(OrderStatus.CONFIRMED);
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setPaidAt(Instant.now());

    // Trừ tồn kho
    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
    inventoryService.deductStock(items);

    List<Long> cartItemProductIds = items.stream().map(OrderItemEntity::getProductId).toList();
    if (!cartItemProductIds.isEmpty()) {
      cartItemRepository.deleteByCartCustomerIdAndProductIdIn(
          order.getUser().getId(), cartItemProductIds);
    }

    // emailService.sendOrderConfirmation(order); // TODO: implement email later
  }

  private void handlePaymentFailed(OrderEntity order, PaymentEntity payment) {
    log.warn("Payment failed for order {}", order.getId());

    order.setStatus(OrderStatus.FAILED);
    payment.setStatus(PaymentStatus.FAILED);

    // Hoàn reserve stock khi thanh toán thất bại (người dùng sẽ phải đặt lại nếu muốn)
    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
    inventoryService.releaseStock(items);

    // KHÔNG xóa cart, KHÔNG trừ kho - user có thể thử lại
  }

  // ============================================================
  // 3. GET STATUS - FE polling sau khi redirect về từ Stripe
  // ============================================================
  @Transactional(readOnly = true)
  public OrderStatusResponse getOrderStatus(Long orderId) {
    Long userId = userHelper.getCurrentUserId();

    OrderEntity order =
        orderJpaRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

    PaymentEntity payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

    return OrderStatusResponse.builder()
        .orderId(order.getId())
        .orderStatus(order.getStatus())
        .paymentStatus(payment.getStatus())
        .build();
  }

  // ============================================================
  // 4. GET ORDERS - Danh sách orders của user (không paging)
  // ============================================================
  @Transactional(readOnly = true)
  public List<OrderDetailResponse> getOrders() {
    Long userId = userHelper.getCurrentUserId();
    List<OrderEntity> orders = orderJpaRepository.findByUserId(userId);
    return orders.stream().map(this::toDetailResponse).toList();
  }

  // ============================================================
  // 4.1 GET MY ORDERS PAGINATED - Danh sách orders của user hiện tại
  // ============================================================
  @Transactional(readOnly = true)
  public Pagination<OrderDetailResponse> getMyOrdersPaginated(
      int page, int size, OrderStatus status) {
    Long userId = userHelper.getCurrentUserId();
    Page<OrderEntity> orderPage;

    if (status != null) {
      orderPage =
          orderJpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
              userId, status, PageRequest.of(page, size));
    } else {
      orderPage =
          orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    return Pagination.<OrderDetailResponse>builder()
        .data(orderPage.getContent().stream().map(this::toDetailResponse).toList())
        .totalElements(orderPage.getTotalElements())
        .totalPages(orderPage.getTotalPages())
        .currentPage(page)
        .build();
  }

  // ============================================================
  // 4.2 GET ALL ORDERS PAGINATED (ADMIN) - Admin xem toàn bộ đơn hàng
  // ============================================================
  @Transactional(readOnly = true)
  public Pagination<OrderDetailResponse> getAllOrdersPaginatedForAdmin(
      int page, int size, OrderStatus status) {
    Page<OrderEntity> orderPage;

    if (status != null) {
      orderPage =
          orderJpaRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
    } else {
      orderPage = orderJpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    return Pagination.<OrderDetailResponse>builder()
        .data(orderPage.getContent().stream().map(this::toDetailResponse).toList())
        .totalElements(orderPage.getTotalElements())
        .totalPages(orderPage.getTotalPages())
        .currentPage(page)
        .build();
  }

  // ============================================================
  // 5. GET ORDER DETAIL - Chi tiết một order
  // ============================================================
  @Transactional(readOnly = true)
  public OrderDetailResponse getOrderDetail(Long orderId) {
    Long userId = userHelper.getCurrentUserId();
    UserEntity user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    OrderEntity order;
    if (user.getRole() == UserRole.ADMIN) {
      order =
          orderJpaRepository
              .findById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    } else {
      order =
          orderJpaRepository
              .findByIdAndUserId(orderId, userId)
              .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
    return toDetailResponse(order);
  }

  // ============================================================
  // Helpers
  // ============================================================
  private OrderDetailResponse toDetailResponse(OrderEntity order) {
    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());

    List<OrderDetailResponse.OrderItemDto> itemDtos =
        items.stream()
            .map(
                item ->
                    OrderDetailResponse.OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productImage(
                            item.getProductImage() != null
                                ? s3Adapter.getUrl(item.getProductImage())
                                : null)
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
            .toList();

    PaymentEntity payment = paymentRepository.findByOrderId(order.getId()).orElse(null);

    return OrderDetailResponse.builder()
        .orderId(order.getId())
        .orderStatus(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .note(order.getNote())
        .createdAt(order.getCreatedAt())
        .shippingName(order.getShippingName())
        .shippingPhone(order.getShippingPhone())
        .shippingAddress(order.getShippingAddress())
        .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
        .paymentStatus(payment != null ? payment.getStatus() : null)
        .paidAt(payment != null ? payment.getPaidAt() : null)
        .items(itemDtos)
        .build();
  }

  @Transactional
  public void cancelOrder(Long orderId) {
    Long userId = userHelper.getCurrentUserId();
    UserEntity user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    OrderEntity order;
    if (user.getRole() == UserRole.ADMIN) {
      order =
          orderJpaRepository
              .findById(orderId)
              .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    } else {
      order =
          orderJpaRepository
              .findByIdAndUserId(orderId, userId)
              .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    // Chỉ cho phép hủy khi đơn hàng đang PENDING hoặc CONFIRMED
    if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
      throw new RuntimeException("Order cannot be cancelled in status: " + order.getStatus());
    }

    PaymentEntity payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

    // Nếu đã thanh toán thành công, thực hiện hoàn tiền trên Stripe
    if (payment.getStatus() == PaymentStatus.SUCCESS
        && payment.getPaymentMethod() != PaymentMethod.CASH) {
      log.info("Initiating refund for order: {}", orderId);
      paymentGatewayService.refund(payment.getTransactionId());
      payment.setStatus(PaymentStatus.REFUNDED);
    } else {
      payment.setStatus(PaymentStatus.FAILED);
    }

    // Hoàn lại tồn kho (dành cho cả đơn PENDING và CONFIRMED)
    List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
    inventoryService.releaseStock(items);

    order.setStatus(OrderStatus.CANCELLED);

    orderJpaRepository.save(order);
    paymentRepository.save(payment);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reconcileSingleOrder(Long orderId) {
    OrderEntity order =
        orderJpaRepository
            .findById(orderId)
            .orElseThrow(
                () -> new RuntimeException("Order not found during reconciliation: " + orderId));

    // Guard: re-check status inside the new transaction to avoid race conditions
    if (order.getStatus() != OrderStatus.PENDING) {
      log.debug(
          "Reconciliation: order {} already in status {}, skipping", orderId, order.getStatus());
      return;
    }

    PaymentEntity payment = order.getPayment();
    if (payment == null || payment.getTransactionId() == null) {
      log.warn("Reconciliation: order {} has no payment/transactionId, skipping", orderId);
      return;
    }

    String sessionId = payment.getTransactionId();
    log.info("Reconciliation: checking Stripe session {} for order {}", sessionId, orderId);

    Session session = stripeAdapter.retrieveSession(sessionId);
    String status = session.getStatus(); // "complete" | "expired" | "open"

    switch (status) {
      case "complete" -> {
        log.info(
            "Reconciliation: session {} is complete — marking order {} CONFIRMED",
            sessionId,
            orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(Instant.now());

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        inventoryService.deductStock(items);

        orderJpaRepository.save(order);
        paymentRepository.save(payment);
      }
      case "expired" -> {
        log.warn("Reconciliation: session {} expired — cancelling order {}", sessionId, orderId);
        order.setStatus(OrderStatus.CANCELLED);
        payment.setStatus(PaymentStatus.FAILED);

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        inventoryService.releaseStock(items);

        orderJpaRepository.save(order);
        paymentRepository.save(payment);
      }
      default ->
          log.warn(
              "Reconciliation: session {} status={} for order {} — no action taken",
              sessionId,
              status,
              orderId);
    }
  }
}
