package com.spring.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.CartItemEntity;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.CartItemStatus;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.CartItemRepository;
import com.spring.backend.repository.CartRepository;
import com.spring.backend.repository.OrderItemRepository;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.repository.PaymentRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Order Service Integration Tests")
class OrderServiceIT extends BaseIntegrationTest {

  @Autowired private OrderService orderService;
  @Autowired private OrderRepository orderRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private CartRepository cartRepository;
  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    orderItemRepository.deleteAll();
    paymentRepository.deleteAll();
    orderRepository.deleteAll();
    cartItemRepository.deleteAll();
    cartRepository.deleteAll();
    productRepository.deleteAll();
    userRepository.deleteAll();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void checkout_createsOrderItemsFromCart() {
    UserEntity customer = createUser("checkout-user", UserRole.CUSTOMER);
    authenticateAs(customer);
    ProductEntity product = saveProduct(ProductStatus.NEW, 10, 0);
    CartItemEntity cartItem = createCartItem(customer, product, 3);

    when(paymentGatewayService.createPaymentUrl(any(), any())).thenReturn(null);

    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(cartItem.getId()));
    request.setPaymentMethod(PaymentMethod.CASH);
    request.setShippingName("John Doe");
    request.setShippingPhone("0123456789");
    request.setShippingAddress("123 Main St");

    CheckoutResponse response = orderService.checkout(request);
    OrderEntity created = orderRepository.findById(response.getOrderId()).orElseThrow();

    List<OrderItemEntity> savedItems = orderItemRepository.findByOrderId(created.getId());
    assertEquals(1, savedItems.size());
    OrderItemEntity saved = savedItems.get(0);
    assertEquals(product.getName(), saved.getProductName());
    assertEquals(3, saved.getQuantity());
    BigDecimal expectedSubtotal =
        BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(3)).setScale(2);
    assertEquals(expectedSubtotal, saved.getSubtotal());

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(3, refreshed.getReservedQty());
  }

  @Test
  void getOrderDetail_returnsPaymentAndItems() {
    UserEntity customer = createUser("detail-user", UserRole.CUSTOMER);
    authenticateAs(customer);
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 1);
    OrderEntity order = createOrder(customer, OrderStatus.PENDING, BigDecimal.valueOf(200));
    orderRepository.save(order);
    OrderItemEntity item = createOrderItem(order, product, 1);
    orderItemRepository.save(item);
    PaymentEntity payment =
        createPayment(order, PaymentMethod.CASH, PaymentStatus.SUCCESS, "px-123", Instant.now());
    paymentRepository.save(payment);

    OrderDetailResponse detail = orderService.getOrderDetail(order.getId());
    assertEquals(order.getId(), detail.getOrderId());
    assertEquals(PaymentStatus.SUCCESS, detail.getPaymentStatus());
    assertEquals(PaymentMethod.CASH, detail.getPaymentMethod());
    assertEquals(1, detail.getItems().size());
    assertEquals(product.getId(), detail.getItems().get(0).getProductId());
  }

  @Test
  void cancelOrder_pendingOrder_failsPaymentWithoutRefund() {
    UserEntity customer = createUser("cancel-user", UserRole.CUSTOMER);
    authenticateAs(customer);
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 1);
    OrderEntity order = createOrder(customer, OrderStatus.PENDING, BigDecimal.valueOf(100));
    orderRepository.save(order);
    OrderItemEntity item = createOrderItem(order, product, 1);
    orderItemRepository.save(item);
    PaymentEntity payment =
        createPayment(order, PaymentMethod.CASH, PaymentStatus.PENDING, null, null);
    paymentRepository.save(payment);

    assertDoesNotThrow(() -> orderService.cancelOrder(order.getId()));

    assertEquals(
        OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    assertEquals(
        PaymentStatus.FAILED,
        paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
    assertEquals(0, productRepository.findById(product.getId()).orElseThrow().getReservedQty());
    verify(paymentGatewayService, never()).refund(anyString());
  }

  @Test
  void cancelOrder_adminConfirmedOrder_refundsAndCancels() {
    UserEntity admin = createUser("admin-user", UserRole.ADMIN);
    authenticateAs(admin);
    UserEntity owner = createUser("owner-user", UserRole.CUSTOMER);
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 1);
    OrderEntity order = createOrder(owner, OrderStatus.CONFIRMED, BigDecimal.valueOf(150));
    orderRepository.save(order);
    OrderItemEntity item = createOrderItem(order, product, 1);
    orderItemRepository.save(item);
    PaymentEntity payment =
        createPayment(
            order, PaymentMethod.STRIPE, PaymentStatus.SUCCESS, "session-1", Instant.now());
    paymentRepository.save(payment);

    assertDoesNotThrow(() -> orderService.cancelOrder(order.getId()));

    assertEquals(
        OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    assertEquals(
        PaymentStatus.REFUNDED,
        paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
    verify(paymentGatewayService).refund("session-1");
  }

  @Test
  void handlePaymentExpired_releasesStockAndMarksOrderCancelled() throws Exception {
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 2);
    UserEntity user = createUser("expired-user", UserRole.CUSTOMER);
    OrderEntity order = createOrder(user, OrderStatus.PENDING, BigDecimal.valueOf(200));
    orderRepository.save(order);
    OrderItemEntity item = createOrderItem(order, product, 2);
    orderItemRepository.save(item);
    PaymentEntity payment =
        createPayment(order, PaymentMethod.STRIPE, PaymentStatus.PENDING, "exp-sess", null);
    paymentRepository.save(payment);

    when(paymentGatewayService.verifySignature(anyString(), anyString())).thenReturn(true);
    when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);

    String payload =
        objectMapper.writeValueAsString(
            Map.of(
                "type",
                "checkout.session.expired",
                "data",
                Map.of("object", Map.of("id", payment.getTransactionId(), "status", "expired"))));

    orderService.handleWebhook("sig", payload);

    assertEquals(
        OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    assertEquals(
        PaymentStatus.FAILED,
        paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
    assertEquals(0, productRepository.findById(product.getId()).orElseThrow().getReservedQty());
  }

  @Test
  void cancelOrder_invalidStatus_throws() {
    UserEntity customer = createUser("locked-user", UserRole.CUSTOMER);
    authenticateAs(customer);
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 1);
    OrderEntity order = createOrder(customer, OrderStatus.FAILED, BigDecimal.valueOf(50));
    orderRepository.save(order);
    OrderItemEntity item = createOrderItem(order, product, 1);
    orderItemRepository.save(item);
    PaymentEntity payment =
        createPayment(order, PaymentMethod.CASH, PaymentStatus.PENDING, null, null);
    paymentRepository.save(payment);

    RuntimeException thrown =
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(order.getId()));
    assertEquals("Order cannot be cancelled in status: FAILED", thrown.getMessage());
  }

  private void authenticateAs(UserEntity user) {
    UserDetailsCustom details =
        UserDetailsCustom.builder()
            .id(user.getId())
            .username(user.getUsername())
            .password("pw")
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(details, null, List.of()));
  }

  private CartItemEntity createCartItem(UserEntity user, ProductEntity product, int quantity) {
    CartEntity cart = CartEntity.builder().customer(user).build();
    cartRepository.save(cart);
    CartItemEntity cartItem =
        CartItemEntity.builder()
            .cart(cart)
            .product(product)
            .price(BigDecimal.valueOf(product.getPrice()))
            .quantity(quantity)
            .status(CartItemStatus.PENDING)
            .build();
    return cartItemRepository.save(cartItem);
  }

  private OrderEntity createOrder(UserEntity user, OrderStatus status, BigDecimal amount) {
    return OrderEntity.builder()
        .user(user)
        .status(status)
        .totalAmount(amount)
        .shippingAddress("street")
        .shippingName("name")
        .shippingPhone("098")
        .note("note")
        .build();
  }

  private OrderItemEntity createOrderItem(OrderEntity order, ProductEntity product, int quantity) {
    return OrderItemEntity.builder()
        .order(order)
        .product(product)
        .productName(product.getName())
        .quantity(quantity)
        .unitPrice(BigDecimal.valueOf(product.getPrice()))
        .subtotal(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity)))
        .build();
  }

  private PaymentEntity createPayment(
      OrderEntity order,
      PaymentMethod method,
      PaymentStatus status,
      String transactionId,
      Instant paidAt) {
    return PaymentEntity.builder()
        .order(order)
        .paymentMethod(method)
        .status(status)
        .amount(order.getTotalAmount())
        .transactionId(transactionId)
        .paidAt(paidAt)
        .build();
  }

  private ProductEntity saveProduct(ProductStatus status, int stockQty, int reservedQty) {
    ProductEntity product =
        ProductEntity.builder().name("Order product").price(100).status(status).build();
    product.setStockQty(stockQty);
    product.setReservedQty(reservedQty);
    return productRepository.save(product);
  }

  private UserEntity createUser(String username, UserRole role) {
    UserEntity user =
        UserEntity.builder()
            .username(username)
            .password("secret")
            .email(username + "@example.com")
            .cardId("CARD-" + username)
            .phone("0900000000")
            .role(role)
            .build();
    return userRepository.save(user);
  }
}
