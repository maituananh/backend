package com.spring.backend.controller.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.entity.*;
import com.spring.backend.enums.*;
import com.spring.backend.repository.*;
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

  @Autowired private OrderRepository orderRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private CartRepository cartRepository;
  @Autowired private CartItemRepository cartItemRepository;

  private UserEntity testUser;
  private UserEntity adminUser;
  private ProductEntity testProduct;
  private CartItemEntity cartItem;

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

    when(paymentGatewayService.verifySignature(any(), any())).thenReturn(true);
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
