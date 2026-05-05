package com.spring.backend.controller.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.domain.enums.*;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.infrastructure.entity.*;
import com.spring.backend.infrastructure.repository.*;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Order Controller Integration Tests")
class OrderControllerIT extends BaseIntegrationTest {

  @Autowired private OrderJpaRepository orderRepository;
  @Autowired private OrderItemJpaRepository orderItemRepository;
  @Autowired private PaymentJpaRepository paymentRepository;
  @Autowired private UserJpaRepository userRepository;
  @Autowired private ProductJpaRepository productRepository;
  @Autowired private CartJpaRepository cartRepository;
  @Autowired private CartItemJpaRepository cartItemRepository;
  @Autowired private ImageJpaRepository imageRepository;

  private UserEntity testUser;
  private UserEntity adminUser;
  private ProductEntity testProduct;
  private CartItemEntity cartItem;

  @BeforeEach
  void setUp() {
    imageRepository.deleteAll();
    orderItemRepository.deleteAll();
    paymentRepository.deleteAll();
    orderRepository.deleteAll();
    cartItemRepository.deleteAll();
    cartRepository.deleteAll();
    productRepository.deleteAll();
    userRepository.deleteAll();

    SecurityContextHolder.clearContext();

    testUser =
        UserEntity.builder()
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .cardId("CARD123")
            .phone("0123456789")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    userRepository.save(testUser);

    adminUser =
        UserEntity.builder()
            .username("admin")
            .password("password")
            .email("admin@example.com")
            .cardId("CARD456")
            .phone("0987654321")
            .role(UserRole.ADMIN)
            .isActive(true)
            .build();
    userRepository.save(adminUser);

    testProduct =
        (ProductEntity)
            ProductEntity.builder()
                .name("Test Product")
                .price(100000)
                .stockQty(10)
                .availableQty(10)
                .status(ProductStatus.NEW)
                .build();
    productRepository.save(testProduct);

    CartEntity cart = CartEntity.builder().customer(testUser).build();
    cartRepository.save(cart);

    cartItem =
        CartItemEntity.builder()
            .cart(cart)
            .product(testProduct)
            .price(BigDecimal.valueOf(testProduct.getPrice()))
            .quantity(1)
            .status(CartItemStatus.PENDING)
            .build();
    cartItemRepository.save(cartItem);
  }

  private UserDetailsCustom getUserDetails(UserEntity user) {
    return UserDetailsCustom.builder()
        .id(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .build();
  }

  @Test
  @DisplayName("POST /api/orders/checkout - returns 200 and creates order (CASH)")
  void checkout_cash_returns200() throws Exception {
    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(cartItem.getId()));
    request.setPaymentMethod(PaymentMethod.CASH);
    request.setShippingName("John Doe");
    request.setShippingPhone("0987654321");
    request.setShippingAddress("123 Street");

    when(paymentGatewayService.createPaymentUrl(any(), any())).thenReturn(null);

    mockMvc
        .perform(
            post("/api/orders/checkout")
                .with(user(getUserDetails(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").exists())
        .andExpect(jsonPath("$.paymentUrl").isEmpty())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("POST /api/orders/checkout - returns 200 and creates order (STRIPE)")
  void checkout_stripe_returns200() throws Exception {
    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(cartItem.getId()));
    request.setPaymentMethod(PaymentMethod.STRIPE);
    request.setShippingName("John Doe");
    request.setShippingPhone("0987654321");
    request.setShippingAddress("123 Street");

    when(paymentGatewayService.createPaymentUrl(any(), any()))
        .thenReturn("http://stripe-session-url");

    mockMvc
        .perform(
            post("/api/orders/checkout")
                .with(user(getUserDetails(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").exists())
        .andExpect(jsonPath("$.paymentUrl").value("http://stripe-session-url"));
  }

  @Test
  @DisplayName("POST /api/orders/checkout - returns 500 when user not found (Line 50)")
  void checkout_userNotFound_returns500() throws Exception {
    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(cartItem.getId()));
    request.setPaymentMethod(PaymentMethod.CASH);
    request.setShippingName("John Doe");
    request.setShippingPhone("0987654321");
    request.setShippingAddress("123 Street");

    UserDetailsCustom nonExistentUser =
        UserDetailsCustom.builder().id(9999L).username("ghost").password("pass").build();

    mockMvc
        .perform(
            post("/api/orders/checkout")
                .with(user(nonExistentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  @DisplayName("POST /api/orders/checkout - reaches line 95 when product has images")
  void checkout_productWithImages_hitsLine95() throws Exception {
    // Add image to product
    ImageEntity image = ImageEntity.builder().fileName("product-image.jpg").build();
    imageRepository.save(image);
    testProduct.setImages(new java.util.ArrayList<>(List.of(image)));
    productRepository.save(testProduct);

    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(cartItem.getId()));
    request.setPaymentMethod(PaymentMethod.CASH);
    request.setShippingName("John Doe");
    request.setShippingPhone("0987654321");
    request.setShippingAddress("123 Street");

    when(paymentGatewayService.createPaymentUrl(any(), any())).thenReturn(null);

    mockMvc
        .perform(
            post("/api/orders/checkout")
                .with(user(getUserDetails(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());

    // Verify order item has image filename
    List<OrderItemEntity> orderItems = orderItemRepository.findAll();
    assert !orderItems.isEmpty();
    assert "product-image.jpg".equals(orderItems.get(0).getProductImage());
  }

  @Test
  @DisplayName("GET /api/orders/{orderId}/status - returns order status")
  void getStatus_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.PENDING);

    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .status(PaymentStatus.PENDING)
            .amount(order.getTotalAmount())
            .paymentMethod(PaymentMethod.STRIPE)
            .build();
    paymentRepository.save(payment);

    mockMvc
        .perform(
            get("/api/orders/" + order.getId() + "/status").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(order.getId()))
        .andExpect(jsonPath("$.orderStatus").value("PENDING"))
        .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
  }

  @Test
  @DisplayName("GET /api/orders/{orderId}/status - returns 500 when order not found (Line 245)")
  void getStatus_orderNotFound_returns500() throws Exception {
    mockMvc
        .perform(get("/api/orders/9999/status").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Order not found: 9999"));
  }

  @Test
  @DisplayName("GET /api/orders/{orderId}/status - returns 500 when payment not found (Line 250)")
  void getStatus_paymentNotFound_returns500() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.PENDING);
    // Do NOT create payment

    mockMvc
        .perform(
            get("/api/orders/" + order.getId() + "/status").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Payment not found for order: " + order.getId()));
  }

  @Test
  @DisplayName("POST /api/payment/webhook - processes stripe webhook")
  void webhook_processesEvent() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.PENDING);

    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .status(PaymentStatus.PENDING)
            .amount(order.getTotalAmount())
            .paymentMethod(PaymentMethod.STRIPE)
            .transactionId("sess_123")
            .build();
    paymentRepository.save(payment);

    String payload =
        "{ \"type\": \"checkout.session.completed\", \"data\": { \"object\": { \"id\": \"sess_123\" } } }";

    Event mockEvent = org.mockito.Mockito.mock(Event.class);
    org.mockito.Mockito.when(mockEvent.getId()).thenReturn("evt_valid_123");
    org.mockito.Mockito.when(mockEvent.getType()).thenReturn("checkout.session.completed");
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenReturn(mockEvent);
    when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "fake-sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andDo(print())
        .andExpect(status().isOk());

    OrderEntity updatedOrder = orderRepository.findById(order.getId()).get();
    assert updatedOrder.getStatus() == OrderStatus.CONFIRMED;
  }

  @Test
  @DisplayName(
      "POST /api/payment/webhook - returns 400 on invalid JSON (now caught by signature verification)")
  void webhook_invalidJson_returns500() throws Exception {
    String invalidPayload = "not a json";

    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenThrow(new SignatureVerificationException("bad sig", "sig-header"));

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "fake-sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/payment/webhook - returns 500 when payment not found")
  void webhook_paymentNotFound_returns500() throws Exception {
    String payload =
        "{ \"type\": \"checkout.session.completed\", \"data\": { \"object\": { \"id\": \"non_existent_sess\" } } }";

    Event mockEvent = org.mockito.Mockito.mock(Event.class);
    org.mockito.Mockito.when(mockEvent.getId()).thenReturn("evt_xyz");
    org.mockito.Mockito.when(mockEvent.getType()).thenReturn("checkout.session.completed");
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenReturn(mockEvent);
    when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "fake-sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Payment not found: non_existent_sess"));
  }

  @Test
  @DisplayName("POST /api/payment/webhook - logs warning on unhandled event")
  void webhook_unhandledEvent_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.PENDING);
    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .status(PaymentStatus.PENDING)
            .amount(order.getTotalAmount())
            .paymentMethod(PaymentMethod.STRIPE)
            .transactionId("sess_456")
            .build();
    paymentRepository.save(payment);

    String payload =
        "{ \"type\": \"some.unhandled.event\", \"data\": { \"object\": { \"id\": \"sess_456\" } } }";

    Event mockEvent = org.mockito.Mockito.mock(Event.class);
    org.mockito.Mockito.when(mockEvent.getId()).thenReturn("evt_unhandled_456");
    org.mockito.Mockito.when(mockEvent.getType()).thenReturn("some.unhandled.event");
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenReturn(mockEvent);
    when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "fake-sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andDo(print())
        .andExpect(status().isOk());

    // Order status should still be PENDING
    OrderEntity updatedOrder = orderRepository.findById(order.getId()).get();
    assert updatedOrder.getStatus() == OrderStatus.PENDING;
  }

  @Test
  @DisplayName("POST /api/payment/webhook returns 400 for invalid Stripe signature")
  void webhook_invalidSignature_returns400() throws Exception {
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenThrow(new SignatureVerificationException("bad sig", "sig-header"));

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "t=bad,v1=bad")
                .contentType(MediaType.TEXT_PLAIN)
                .content("{\"id\":\"evt_bad\",\"type\":\"checkout.session.completed\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/payment/webhook returns 200 for duplicate event (idempotency)")
  void webhook_duplicateEvent_returns200() throws Exception {
    Event mockEvent = org.mockito.Mockito.mock(Event.class);
    org.mockito.Mockito.when(mockEvent.getId()).thenReturn("evt_dup123");
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenReturn(mockEvent);

    // The real OrderService will throw DuplicateWebhookEventException when it sees a dup
    // We test this end-to-end: controller must return 200 not 500
    // Insert a payment with this stripeEventId to trigger the dup path
    OrderEntity order = createOrder(testUser, OrderStatus.CONFIRMED);
    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .status(PaymentStatus.SUCCESS)
            .amount(order.getTotalAmount())
            .paymentMethod(PaymentMethod.STRIPE)
            .transactionId("sess_dup_123")
            .stripeEventId("evt_dup123")
            .build();
    paymentRepository.save(payment);
    when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "t=1,v1=abc")
                .contentType(MediaType.TEXT_PLAIN)
                .content("{\"id\":\"evt_dup123\",\"type\":\"checkout.session.completed\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/orders/all - returns user orders")
  void getOrders_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.CONFIRMED);

    mockMvc
        .perform(get("/api/orders/all").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].orderId").value(order.getId()));
  }

  @Test
  @DisplayName("GET /api/orders - returns paged user orders")
  void getMyOrdersPaginated_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.CONFIRMED);

    mockMvc
        .perform(get("/api/orders").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("GET /api/orders - returns 200 with status filter (Line 279)")
  void getMyOrdersPaginated_withStatus_returns200() throws Exception {
    createOrder(testUser, OrderStatus.CONFIRMED);
    createOrder(testUser, OrderStatus.CANCELLED);

    mockMvc
        .perform(
            get("/api/orders").param("status", "CONFIRMED").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("GET /api/admin/orders - returns paged all orders for admin")
  void getAllOrdersPaginatedForAdmin_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.CONFIRMED);

    mockMvc
        .perform(get("/api/admin/orders").with(user(getUserDetails(adminUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("GET /api/admin/orders - returns 200 with status filter (Line 304)")
  void getAllOrdersPaginatedForAdmin_withStatus_returns200() throws Exception {
    createOrder(testUser, OrderStatus.CONFIRMED);
    createOrder(testUser, OrderStatus.CANCELLED);

    mockMvc
        .perform(
            get("/api/admin/orders")
                .param("status", "CANCELLED")
                .with(user(getUserDetails(adminUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("GET /api/orders/{orderId} - returns order detail")
  void getOrderDetail_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.CONFIRMED);

    mockMvc
        .perform(get("/api/orders/" + order.getId()).with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(order.getId()));
  }

  @Test
  @DisplayName("GET /api/orders/{orderId} - returns 500 when user not found (Line 325)")
  void getOrderDetail_userNotFound_returns500() throws Exception {
    UserDetailsCustom nonExistentUser =
        UserDetailsCustom.builder().id(9999L).username("ghost").password("pass").build();

    mockMvc
        .perform(get("/api/orders/1").with(user(nonExistentUser)))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  @DisplayName("GET /api/orders/{orderId} - returns 500 when admin order not found (Line 332)")
  void getOrderDetail_admin_orderNotFound_returns500() throws Exception {
    mockMvc
        .perform(get("/api/orders/9999").with(user(getUserDetails(adminUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Order not found: 9999"));
  }

  @Test
  @DisplayName(
      "GET /api/orders/{orderId} - returns 500 when user order not found/unauthorized (Line 337)")
  void getOrderDetail_customer_orderNotFound_returns500() throws Exception {
    // Create order for admin, try to access as customer
    OrderEntity adminOrder = createOrder(adminUser, OrderStatus.CONFIRMED);

    mockMvc
        .perform(get("/api/orders/" + adminOrder.getId()).with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Order not found: " + adminOrder.getId()));
  }

  @Test
  @DisplayName("POST /api/orders/{orderId}/cancel - cancels order")
  void cancelOrder_returns200() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.PENDING);

    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .status(PaymentStatus.PENDING)
            .amount(order.getTotalAmount())
            .paymentMethod(PaymentMethod.CASH)
            .build();
    paymentRepository.save(payment);

    mockMvc
        .perform(
            post("/api/orders/" + order.getId() + "/cancel").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk());

    OrderEntity updatedOrder = orderRepository.findById(order.getId()).get();
    assert updatedOrder.getStatus() == OrderStatus.CANCELLED;
  }

  @Test
  @DisplayName("POST /api/orders/{orderId}/cancel - returns 500 when user not found (Line 387)")
  void cancelOrder_userNotFound_returns500() throws Exception {
    UserDetailsCustom nonExistentUser =
        UserDetailsCustom.builder().id(9999L).username("ghost").password("pass").build();

    mockMvc
        .perform(post("/api/orders/1/cancel").with(user(nonExistentUser)))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  @DisplayName(
      "POST /api/orders/{orderId}/cancel - returns 500 when admin order not found (Line 394)")
  void cancelOrder_admin_orderNotFound_returns500() throws Exception {
    mockMvc
        .perform(post("/api/orders/9999/cancel").with(user(getUserDetails(adminUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Order not found: 9999"));
  }

  @Test
  @DisplayName(
      "POST /api/orders/{orderId}/cancel - returns 500 when user order not found (Line 399)")
  void cancelOrder_customer_orderNotFound_returns500() throws Exception {
    mockMvc
        .perform(post("/api/orders/9999/cancel").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Order not found: 9999"));
  }

  @Test
  @DisplayName("POST /api/orders/{orderId}/cancel - returns 500 when payment not found (Line 410)")
  void cancelOrder_paymentNotFound_returns500() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.PENDING);
    // No payment created

    mockMvc
        .perform(
            post("/api/orders/" + order.getId() + "/cancel").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Payment not found for order: " + order.getId()));
  }

  @Test
  @DisplayName("POST /api/orders/{orderId}/cancel - no refund for CASH success (Line 414)")
  void cancelOrder_cashSuccess_noStripeRefund() throws Exception {
    OrderEntity order = createOrder(testUser, OrderStatus.CONFIRMED);
    PaymentEntity payment =
        PaymentEntity.builder()
            .order(order)
            .status(PaymentStatus.SUCCESS)
            .amount(order.getTotalAmount())
            .paymentMethod(PaymentMethod.CASH)
            .build();
    paymentRepository.save(payment);

    mockMvc
        .perform(
            post("/api/orders/" + order.getId() + "/cancel").with(user(getUserDetails(testUser))))
        .andDo(print())
        .andExpect(status().isOk());

    PaymentEntity updatedPayment = paymentRepository.findById(payment.getId()).get();
    assert updatedPayment.getStatus() == PaymentStatus.FAILED;
    // Stripe refund should NOT have been called (verified by lack of interaction with mock if we
    // used Verifiable)
  }

  private OrderEntity createOrder(UserEntity user, OrderStatus status) {
    OrderEntity order =
        OrderEntity.builder()
            .user(user)
            .status(status)
            .totalAmount(BigDecimal.valueOf(100000))
            .shippingName("John Doe")
            .shippingAddress("123 Street")
            .shippingPhone("0987654321")
            .build();
    return orderRepository.save(order);
  }
}
