package com.spring.backend.service;

import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.entity.*;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final PaymentRepository paymentRepository;
  private final CartItemRepository cartItemRepository;
  private final PaymentGatewayService paymentGatewayService;
  private final InventoryService inventoryService;
  private final EmailService emailService;
  private final UserHelper userHelper;
  private final UserRepository userRepository;

  // ============================================================
  // 1. CHECKOUT - Tạo order từ các cart item được chọn
  // ============================================================
  public CheckoutResponse checkout(CheckoutRequest request) {
    Long userId = userHelper.getCurrentUserId();
    UserEntity userEntity = userRepository.findById(userId).orElseThrow();

    // Validate cart items
    List<CartItemEntity> cartItems =
        cartItemRepository.findByIdInAndCustomerId(request.getCartItemIds(), userId);

    if (cartItems.isEmpty()) {
      throw new RuntimeException("No items selected");
    }
    if (cartItems.size() != request.getCartItemIds().size()) {
      throw new RuntimeException("Some items are invalid or not yours");
    }

    // Kiểm tra tồn kho trước khi tạo order
    inventoryService.validateStock(cartItems);

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
    orderRepository.save(order);

    // Tạo Order Items (snapshot)
    List<OrderItemEntity> orderItems =
        cartItems.stream()
            .map(
                cart ->
                        OrderItemEntity.builder()
                        .order(order)
                        .productId(cart.getProductId())
                        .productName(cart.getProduct().getName())
                        .productImage(cart.getProduct().getImage())
                        .unitPrice(cart.getPrice())
                        .quantity(cart.getQuantity())
                        .subtotal(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                        .build())
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

    // Lấy payment URL từ Gateway
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
  public void handleWebhook(WebhookPayload payload) {

    // Verify chữ ký tránh giả mạo
    if (!paymentGatewayService.verifySignature(payload)) {
      throw new RuntimeException("Invalid signature");
    }

    PaymentEntity payment =
        paymentRepository
            .findByTransactionId(payload.getTransactionId())
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    OrderEntity order = payment.getOrder();

    // Idempotency: tránh xử lý 2 lần
    if (order.getStatus() != OrderStatus.PENDING) {
      log.warn("Order {} already processed, skipping", order.getId());
      return;
    }

    // Lưu raw response để debug
    payment.setGatewayResponse(payload.getRawResponse());

    if (payload.isSuccess()) {
      handlePaymentSuccess(order, payment);
    } else {
      handlePaymentFailed(order, payment);
    }

    orderRepository.save(order);
    paymentRepository.save(payment);
  }

  private void handlePaymentSuccess(OrderEntity order, PaymentEntity payment) {
    log.info("Payment success for order {}", order.getId());

    order.setStatus(OrderStatus.CONFIRMED);
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setPaidAt(Instant.now());

    // Trừ tồn kho
    inventoryService.deductStock(order.getItems());

    // Xóa các cart items đã thanh toán
    List<Long> cartItemIds = order.getItems().stream().map(OrderItemEntity::getProductId).toList();
    cartItemRepository.deleteByIdInAndCartCustomerId(cartItemIds, order.getUserId());

    // Gửi email xác nhận
    emailService.sendOrderConfirmation(order);
  }

  private void handlePaymentFailed(OrderEntity order, PaymentEntity payment) {
    log.warn("Payment failed for order {}", order.getId());

    order.setStatus(OrderStatus.FAILED);
    payment.setStatus(PaymentStatus.FAILED);

    // KHÔNG xóa cart, KHÔNG trừ kho
    // User có thể thử lại bằng cách tạo order mới
  }

  // ============================================================
  // 3. GET STATUS - FE polling sau khi redirect về
  // ============================================================
  @Transactional(readOnly = true)
  public OrderStatusResponse getOrderStatus(Long orderId) {
    Long userId = userHelper.getCurrentUserId();

    OrderEntity order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    PaymentEntity payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    return OrderStatusResponse.builder()
        .orderId(order.getId())
        .orderStatus(order.getStatus())
        .paymentStatus(payment.getStatus())
        .build();
  }
}
