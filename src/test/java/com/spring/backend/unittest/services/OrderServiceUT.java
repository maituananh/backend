package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.entity.*;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.*;
import com.spring.backend.service.InventoryService;
import com.spring.backend.service.OrderService;
import com.spring.backend.service.PaymentGatewayService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceUT {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private PaymentGatewayService paymentGatewayService;
  @Mock private InventoryService inventoryService;
  @Mock private UserHelper userHelper;
  @Mock private UserRepository userRepository;
  @Mock private S3Adapter s3Adapter;
  @Mock private ObjectMapper objectMapper;
  @InjectMocks private OrderService orderService;

  private UserEntity user;
  private OrderEntity order;
  private PaymentEntity payment;
  private CartItemEntity cartItem;
  private CheckoutRequest checkoutRequest;

  @BeforeEach
  void setUp() {
    user = UserEntity.builder().id(1L).username("testuser").role(UserRole.CUSTOMER).build();
    order =
        OrderEntity.builder()
            .id(100L)
            .user(user)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(200.0))
            .items(new ArrayList<>())
            .build();
    payment =
        PaymentEntity.builder()
            .id(1000L)
            .order(order)
            .amount(BigDecimal.valueOf(200.0))
            .status(PaymentStatus.PENDING)
            .transactionId("tx_123")
            .build();

    ProductEntity product = ProductEntity.builder().id(10L).name("Prod 1").build();
    cartItem =
        CartItemEntity.builder()
            .id(50L)
            .product(product)
            .price(BigDecimal.valueOf(100.0))
            .quantity(2)
            .build();

    checkoutRequest = new CheckoutRequest();
    checkoutRequest.setCartItemIds(List.of(50L));
    checkoutRequest.setPaymentMethod(PaymentMethod.STRIPE);
    checkoutRequest.setShippingName("John Doe");
    checkoutRequest.setShippingPhone("12345");
    checkoutRequest.setShippingAddress("Main St");
  }

  @Nested
  @DisplayName("checkout Tests")
  class CheckoutTests {
    @Test
    @DisplayName("should create order successfully")
    void shouldCheckoutSuccessfully() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), anyLong()))
          .thenReturn(List.of(cartItem));
      when(paymentGatewayService.createPaymentUrl(any(), any())).thenReturn("http://checkout.url");

      // Act
      CheckoutResponse result = orderService.checkout(checkoutRequest);

      // Assert
      assertThat(result.getPaymentUrl()).isEqualTo("http://checkout.url");
      assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
      verify(inventoryService).validateStock(anyList());
      verify(inventoryService).reserveStock(anyList());
      verify(orderRepository).save(any(OrderEntity.class));
      verify(orderItemRepository).saveAll(anyList());
      verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("should throw error when items empty")
    void shouldThrowErrorWhenEmptyItems() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), anyLong()))
          .thenReturn(Collections.emptyList());

      // Act & Assert
      assertThatThrownBy(() -> orderService.checkout(checkoutRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("No items selected");
    }

    @Test
    @DisplayName("should throw error when some items invalid")
    void shouldThrowErrorWhenInvalidItems() {
      // Arrange
      checkoutRequest.setCartItemIds(List.of(50L, 51L));
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), anyLong()))
          .thenReturn(List.of(cartItem));

      // Act & Assert
      assertThatThrownBy(() -> orderService.checkout(checkoutRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Some items are invalid or not yours");
    }
  }

  @Nested
  @DisplayName("handleWebhook Tests")
  class WebhookTests {
    @Test
    @DisplayName("should handle success payment event")
    void shouldHandleSuccessPayment() throws Exception {
      // Arrange
      WebhookPayload payload = new WebhookPayload();
      payload.setEventType("checkout.session.completed");
      payload.setTransactionId("tx_123");
      String payloadStr = "{}";

      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(payload);
      when(paymentGatewayService.verifySignature(anyString(), anyString())).thenReturn(true);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      // Act
      orderService.handleWebhook("sig", payloadStr);

      // Assert
      assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      verify(inventoryService).deductStock(anyList());
      verify(cartItemRepository).deleteByCartCustomerIdAndProductIdIn(anyLong(), anyList());
    }

    @Test
    @DisplayName("should handle failed payment event")
    void shouldHandleFailedPayment() throws Exception {
      // Arrange
      WebhookPayload payload = new WebhookPayload();
      payload.setEventType("payment_intent.payment_failed");
      payload.setTransactionId("tx_123");
      String payloadStr = "{}";

      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(payload);
      when(paymentGatewayService.verifySignature(anyString(), anyString())).thenReturn(true);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      // Act
      orderService.handleWebhook("sig", payloadStr);

      // Assert
      assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
      verify(inventoryService).releaseStock(anyList());
    }

    @Test
    @DisplayName("should skip processed orders for idempotency")
    void shouldSkipProcessedOrders() throws Exception {
      // Arrange
      order.setStatus(OrderStatus.CONFIRMED);
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");
      String payloadStr = "{}";

      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(payload);
      when(paymentGatewayService.verifySignature(anyString(), anyString())).thenReturn(true);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      // Act
      orderService.handleWebhook("sig", payloadStr);

      // Assert
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw error on invalid signature")
    void shouldThrowOnInvalidSignature() throws Exception {
      // Arrange
      WebhookPayload payload = new WebhookPayload();
      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifySignature(any(), any())).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> orderService.handleWebhook("sig", "{}"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Invalid signature");
    }
  }

  @Nested
  @DisplayName("getOrderDetail Tests")
  class GetOrderDetailTests {
    @Test
    @DisplayName("should return detail for owner")
    void shouldReturnDetailForOwner() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));

      // Act
      OrderDetailResponse result = orderService.getOrderDetail(100L);

      // Assert
      assertThat(result.getOrderId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("should return detail for admin even if not owner")
    void shouldReturnDetailForAdmin() {
      // Arrange
      UserEntity admin = UserEntity.builder().id(2L).role(UserRole.ADMIN).build();
      when(userHelper.getCurrentUserId()).thenReturn(2L);
      when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      // Act
      OrderDetailResponse result = orderService.getOrderDetail(100L);

      // Assert
      assertThat(result.getOrderId()).isEqualTo(100L);
    }
  }

  @Nested
  @DisplayName("cancelOrder Tests")
  class CancelOrderTests {
    @Test
    @DisplayName("should refund and cancel Stripe order")
    void shouldCancelAndRefundStripe() {
      // Arrange
      payment.setStatus(PaymentStatus.SUCCESS);
      payment.setPaymentMethod(PaymentMethod.STRIPE);
      payment.setTransactionId("tx_123");

      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));
      when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

      // Act
      orderService.cancelOrder(100L);

      // Assert
      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
      verify(paymentGatewayService).refund("tx_123");
      verify(inventoryService).releaseStock(anyList());
    }

    @Test
    @DisplayName("should not call Stripe for CASH order cancellation")
    void shouldNotRefundCash() {
      // Arrange
      payment.setStatus(PaymentStatus.PENDING);
      payment.setPaymentMethod(PaymentMethod.CASH);

      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));
      when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

      // Act
      orderService.cancelOrder(100L);

      // Assert
      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
      verify(paymentGatewayService, never()).refund(anyString());
    }

    @Test
    @DisplayName("should throw error if order status not cancellable")
    void shouldThrowOnInvalidStatus() {
      // Arrange
      order.setStatus(OrderStatus.DELIVERED);
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));

      // Act & Assert
      assertThatThrownBy(() -> orderService.cancelOrder(100L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Order cannot be cancelled in status");
    }
  }

  @Test
  @DisplayName("getOrderStatus should return response")
  void shouldReturnOrderStatus() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));
    when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

    // Act
    OrderStatusResponse result = orderService.getOrderStatus(100L);

    // Assert
    assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
  }

  @Test
  @DisplayName("getMyOrdersPaginated should return pagination")
  void shouldReturnPaginatedOrders() {
    // Arrange
    Page<OrderEntity> page = new PageImpl<>(List.of(order));
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
        .thenReturn(page);

    // Act
    Pagination<OrderDetailResponse> result = orderService.getMyOrdersPaginated(0, 10, null);

    // Assert
    assertThat(result.getData()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }
}
