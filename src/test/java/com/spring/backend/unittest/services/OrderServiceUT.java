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
import com.spring.backend.adapter.stripe.StripeAdapter;
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
import com.spring.backend.exception.DuplicateWebhookEventException;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.*;
import com.spring.backend.service.InventoryService;
import com.spring.backend.service.OrderService;
import com.spring.backend.service.PaymentGatewayService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
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
  @Mock private StripeAdapter stripeAdapter;
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

    private Event mockEvent;
    private String payloadStr;
    private WebhookPayload webhookPayload;

    @BeforeEach
    void setUp() throws Exception {
      payloadStr = "{\"id\":\"cs_123\",\"type\":\"checkout.session.completed\"}";
      mockEvent = mock(Event.class);
      when(mockEvent.getId()).thenReturn("evt_test123");
      when(mockEvent.getType()).thenReturn("checkout.session.completed");

      webhookPayload = new WebhookPayload();
      webhookPayload.setEventType("checkout.session.completed");
      webhookPayload.setTransactionId("tx_123");
    }

    @Test
    @DisplayName("should handle success payment event")
    void shouldHandleSuccessPayment() throws Exception {
      when(paymentRepository.findByStripeEventId("evt_test123")).thenReturn(Optional.empty());
      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(webhookPayload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      orderService.handleWebhook(mockEvent, payloadStr);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      verify(inventoryService).deductStock(anyList());
      verify(cartItemRepository).deleteByCartCustomerIdAndProductIdIn(anyLong(), anyList());
    }

    @Test
    @DisplayName("should handle failed payment event")
    void shouldHandleFailedPayment() throws Exception {
      when(mockEvent.getType()).thenReturn("payment_intent.payment_failed");
      when(paymentRepository.findByStripeEventId("evt_test123")).thenReturn(Optional.empty());

      WebhookPayload failedPayload = new WebhookPayload();
      failedPayload.setEventType("payment_intent.payment_failed");
      failedPayload.setTransactionId("tx_123");
      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(failedPayload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      orderService.handleWebhook(mockEvent, payloadStr);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
      verify(inventoryService).releaseStock(anyList());
    }

    @Test
    @DisplayName("should skip processed orders for idempotency (status-level guard)")
    void shouldSkipProcessedOrders() throws Exception {
      order.setStatus(OrderStatus.CONFIRMED);
      when(paymentRepository.findByStripeEventId("evt_test123")).thenReturn(Optional.empty());
      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(webhookPayload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      orderService.handleWebhook(mockEvent, payloadStr);

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw DuplicateWebhookEventException for duplicate stripe event ID")
    void shouldThrowOnDuplicateStripeEventId() {
      PaymentEntity existingPayment = mock(PaymentEntity.class);
      OrderEntity existingOrder = mock(OrderEntity.class);
      when(existingPayment.getOrder()).thenReturn(existingOrder);
      when(existingOrder.getId()).thenReturn(50L);
      when(paymentRepository.findByStripeEventId("evt_test123"))
          .thenReturn(Optional.of(existingPayment));

      assertThatThrownBy(() -> orderService.handleWebhook(mockEvent, payloadStr))
          .isInstanceOf(DuplicateWebhookEventException.class)
          .hasMessageContaining("evt_test123");
    }

    @Test
    @DisplayName("checkout.session.expired — cancels order and releases stock")
    void shouldHandleExpiredSession() throws Exception {
      when(mockEvent.getType()).thenReturn("checkout.session.expired");
      when(paymentRepository.findByStripeEventId("evt_test123")).thenReturn(Optional.empty());

      WebhookPayload expiredPayload = new WebhookPayload();
      expiredPayload.setEventType("checkout.session.expired");
      expiredPayload.setTransactionId("tx_123");
      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(expiredPayload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      orderService.handleWebhook(mockEvent, payloadStr);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
      verify(inventoryService).releaseStock(anyList());
      verify(orderRepository).save(order);
      verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("unknown event type — does not save order or payment (edge case)")
    void unknownEventType_doesNotSaveOrderOrPayment() throws Exception {
      when(mockEvent.getType()).thenReturn("account.updated");
      when(paymentRepository.findByStripeEventId("evt_test123")).thenReturn(Optional.empty());
      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(webhookPayload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      orderService.handleWebhook(mockEvent, payloadStr);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
      verify(orderRepository, never()).save(any());
      verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("valid non-duplicate event — persists stripeEventId on payment (D-01/D-03)")
    void validEvent_persistsStripeEventId() throws Exception {
      when(paymentRepository.findByStripeEventId("evt_test123")).thenReturn(Optional.empty());
      when(objectMapper.readValue(payloadStr, WebhookPayload.class)).thenReturn(webhookPayload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      orderService.handleWebhook(mockEvent, payloadStr);

      assertThat(payment.getStripeEventId()).isEqualTo("evt_test123");
    }
  }

  @Nested
  @DisplayName("reconcileSingleOrder Tests")
  class ReconcileSingleOrderTests {

    @BeforeEach
    void setUp() {
      payment.setTransactionId("sess_test_123");
      order.setPayment(payment);
    }

    @Test
    @DisplayName("session complete — marks CONFIRMED, deducts stock, sets paidAt")
    void sessionComplete_confirmsOrderAndDeductsStock() {
      Session session = mock(Session.class);
      when(session.getStatus()).thenReturn("complete");
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
      when(stripeAdapter.retrieveSession("sess_test_123")).thenReturn(session);
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      orderService.reconcileSingleOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      assertThat(payment.getPaidAt()).isNotNull();
      verify(inventoryService).deductStock(anyList());
      verify(orderRepository).save(order);
      verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("session expired — marks CANCELLED and releases stock")
    void sessionExpired_cancelsOrderAndReleasesStock() {
      Session session = mock(Session.class);
      when(session.getStatus()).thenReturn("expired");
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
      when(stripeAdapter.retrieveSession("sess_test_123")).thenReturn(session);
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(new OrderItemEntity()));

      orderService.reconcileSingleOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
      verify(inventoryService).releaseStock(anyList());
      verify(orderRepository).save(order);
      verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("session open — takes no action (still in progress)")
    void sessionOpen_takesNoAction() {
      Session session = mock(Session.class);
      when(session.getStatus()).thenReturn("open");
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
      when(stripeAdapter.retrieveSession("sess_test_123")).thenReturn(session);

      orderService.reconcileSingleOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
      verify(inventoryService, never()).deductStock(anyList());
      verify(inventoryService, never()).releaseStock(anyList());
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("unknown session status — takes no action and logs warn (edge case)")
    void unknownStatus_takesNoAction() {
      Session session = mock(Session.class);
      when(session.getStatus()).thenReturn("trialing"); // unexpected Stripe status
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
      when(stripeAdapter.retrieveSession("sess_test_123")).thenReturn(session);

      orderService.reconcileSingleOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
      verify(inventoryService, never()).deductStock(anyList());
      verify(inventoryService, never()).releaseStock(anyList());
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("order not found — throws RuntimeException (edge case)")
    void orderNotFound_throwsRuntime() {
      when(orderRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.reconcileSingleOrder(999L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("999");
    }

    @Test
    @DisplayName("order already CONFIRMED — skips immediately (race condition guard)")
    void orderAlreadyConfirmed_skips() {
      order.setStatus(OrderStatus.CONFIRMED);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);

      verifyNoInteractions(stripeAdapter, inventoryService);
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("order already CANCELLED — skips immediately (race condition guard)")
    void orderAlreadyCancelled_skips() {
      order.setStatus(OrderStatus.CANCELLED);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);

      verifyNoInteractions(stripeAdapter, inventoryService);
    }

    @Test
    @DisplayName("no payment on order — skips and logs warn (edge case)")
    void noPayment_skipsAndLogsWarn() {
      order.setPayment(null);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);

      verifyNoInteractions(stripeAdapter, inventoryService);
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("null transactionId — skips and logs warn (edge case)")
    void nullTransactionId_skipsAndLogsWarn() {
      payment.setTransactionId(null);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);

      verifyNoInteractions(stripeAdapter, inventoryService);
    }

    @Test
    @DisplayName("Stripe throws exception — propagates to caller (caller logs and continues)")
    void stripeThrows_propagatesException() {
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
      when(stripeAdapter.retrieveSession("sess_test_123"))
          .thenThrow(new RuntimeException("Stripe API timeout"));

      assertThatThrownBy(() -> orderService.reconcileSingleOrder(100L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Stripe API timeout");
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
