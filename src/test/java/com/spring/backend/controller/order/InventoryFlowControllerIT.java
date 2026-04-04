package com.spring.backend.controller.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.checkout.CheckoutRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@DisplayName("Order Controller Inventory Flows")
class InventoryFlowControllerIT extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ProductRepository productRepository;
  @Autowired private CartRepository cartRepository;
  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private PaymentRepository paymentRepository;

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();
    orderItemRepository.deleteAll();
    orderRepository.deleteAll();
    cartItemRepository.deleteAll();
    cartRepository.deleteAll();
    productRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void checkout_reservesInventory_whenCalledThroughController() throws Exception {
    UserEntity customer = createUser("api-customer", UserRole.CUSTOMER);
    ProductEntity product = saveProduct(ProductStatus.NEW, 10, 0);
    CartItemEntity cartItem = createCartItem(customer, product, 2);

    when(paymentGatewayService.createPaymentUrl(any(), any())).thenReturn(null);

    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(cartItem.getId()));
    request.setPaymentMethod(PaymentMethod.CASH);
    request.setShippingAddress("123 Main St");
    request.setShippingName("John Doe");
    request.setShippingPhone("0909000000");

    mockMvc
        .perform(
            post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(customer)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").exists())
        .andExpect(jsonPath("$.status").value("PENDING"));

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(2, refreshed.getReservedQty());
    assertEquals(product.getStockQty() - 2, refreshed.getAvailableQty());
  }

  @Test
  void cancelOrder_releasesInventory_whenInvokedViaController() throws Exception {
    UserEntity customer = createUser("api-cancel", UserRole.CUSTOMER);
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 2);
    OrderEntity order = createOrder(customer, OrderStatus.PENDING, BigDecimal.valueOf(150));
    orderRepository.save(order);
    OrderItemEntity orderItem = createOrderItem(order, product, 2);
    orderItemRepository.save(orderItem);
    PaymentEntity payment =
        createPayment(order, PaymentMethod.CASH, PaymentStatus.PENDING, null, null);
    paymentRepository.save(payment);

    mockMvc
        .perform(post("/api/orders/" + order.getId() + "/cancel").with(auth(customer)))
        .andExpect(status().isOk());

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(0, refreshed.getReservedQty());
    assertEquals(
        PaymentStatus.FAILED,
        paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
  }

  @Test
  void webhook_expired_releasesInventory_andCancelsOrder() throws Exception {
    UserEntity customer = createUser("api-webhook", UserRole.CUSTOMER);
    ProductEntity product = saveProduct(ProductStatus.NEW, 3, 3);
    OrderEntity order = createOrder(customer, OrderStatus.PENDING, BigDecimal.valueOf(200));
    orderRepository.save(order);
    OrderItemEntity orderItem = createOrderItem(order, product, 3);
    orderItemRepository.save(orderItem);
    PaymentEntity payment =
        createPayment(order, PaymentMethod.STRIPE, PaymentStatus.PENDING, "webhook-sess", null);
    paymentRepository.save(payment);

    when(paymentGatewayService.verifySignature(anyString(), anyString())).thenReturn(true);
    when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);

    Map<String, Object> payload =
        Map.of(
            "type",
            "checkout.session.expired",
            "data",
            Map.of("object", Map.of("id", payment.getTransactionId(), "status", "expired")));

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk());

    assertEquals(
        OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    assertEquals(0, productRepository.findById(product.getId()).orElseThrow().getReservedQty());
  }

  private UserEntity createUser(String username, UserRole role) {
    return userRepository.save(
        UserEntity.builder()
            .username(username)
            .password("pw")
            .email(username + "@example.com")
            .cardId("CARD-" + username)
            .phone("0900000000")
            .role(role)
            .build());
  }

  private ProductEntity saveProduct(ProductStatus status, int stockQty, int reservedQty) {
    ProductEntity product =
        ProductEntity.builder().name("Inventory product").price(100).status(status).build();
    product.setStockQty(stockQty);
    product.setReservedQty(reservedQty);
    return productRepository.save(product);
  }

  private CartItemEntity createCartItem(UserEntity user, ProductEntity product, int quantity) {
    CartEntity cart = cartRepository.save(CartEntity.builder().customer(user).build());
    return cartItemRepository.save(
        CartItemEntity.builder()
            .cart(cart)
            .product(product)
            .price(BigDecimal.valueOf(product.getPrice()))
            .quantity(quantity)
            .status(CartItemStatus.PENDING)
            .build());
  }

  private OrderEntity createOrder(UserEntity user, OrderStatus status, BigDecimal amount) {
    return OrderEntity.builder()
        .user(user)
        .status(status)
        .totalAmount(amount)
        .shippingName("Name")
        .shippingPhone("0900000000")
        .shippingAddress("Address")
        .note("note")
        .build();
  }

  private OrderItemEntity createOrderItem(OrderEntity order, ProductEntity product, int quantity) {
    return OrderItemEntity.builder()
        .order(order)
        .product(product)
        .productName(product.getName())
        .unitPrice(BigDecimal.valueOf(product.getPrice()))
        .quantity(quantity)
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

  private UserDetails userDetails(UserEntity user) {
    return UserDetailsCustom.builder()
        .id(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .build();
  }

  private RequestPostProcessor auth(UserEntity user) {
    return user(userDetails(user));
  }
}
