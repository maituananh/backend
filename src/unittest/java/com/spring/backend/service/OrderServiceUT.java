package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.domain.enums.OrderStatus;
import com.spring.backend.domain.enums.PaymentMethod;
import com.spring.backend.domain.enums.PaymentStatus;
import com.spring.backend.domain.enums.UserRole;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.exception.DuplicateWebhookEventException;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.infrastructure.entity.*;
import com.spring.backend.infrastructure.repository.*;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
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
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceUT {

  @Mock private OrderJpaRepository orderRepository;
  @Mock private OrderItemJpaRepository orderItemRepository;
  @Mock private PaymentJpaRepository paymentRepository;
  @Mock private CartItemJpaRepository cartItemRepository;
  @Mock private PaymentGatewayService paymentGatewayService;
  @Mock private InventoryService inventoryService;
  @Mock private UserHelper userHelper;
  @Mock private UserJpaRepository userRepository;
  @Mock private S3Adapter s3Adapter;
  @Mock private ObjectMapper objectMapper;
  @Mock private StripeAdapter stripeAdapter;

  @InjectMocks private OrderService orderService;

  private final Long userId = 1L;
  private UserEntity user;

  @BeforeEach
  void setUp() {
    user = new UserEntity();
    user.setId(userId);
    user.setRole(UserRole.CUSTOMER);
  }

  @Nested
  @DisplayName("checkout tests")
  class CheckoutTests {
    @Test
    @DisplayName("checkout should work correctly")
    void checkout_Works() {
      CheckoutRequest request = new CheckoutRequest();
      request.setCartItemIds(List.of(1L));
      request.setPaymentMethod(PaymentMethod.STRIPE);

      ProductEntity product = new ProductEntity();
      product.setName("Product 1");

      CartItemEntity cartItem = new CartItemEntity();
      cartItem.setId(1L);
      cartItem.setProduct(product);
      cartItem.setPrice(BigDecimal.TEN);
      cartItem.setQuantity(2);

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), eq(userId)))
          .thenReturn(List.of(cartItem));
      when(paymentGatewayService.createPaymentUrl(any(), any())).thenReturn("http://payment.url");

      CheckoutResponse response = orderService.checkout(request);

      assertThat(response.getPaymentUrl()).isEqualTo("http://payment.url");
      assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
      verify(inventoryService).reserveStock(anyList());
      verify(orderRepository).save(any());
      verify(orderItemRepository).saveAll(anyList());
      verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("checkout should handle product with images")
    void checkout_WithProductImages() {
      CheckoutRequest request = new CheckoutRequest();
      request.setCartItemIds(List.of(1L));

      ImageEntity image = new ImageEntity();
      image.setFileName("img.jpg");

      ProductEntity product = new ProductEntity();
      product.setImages(List.of(image));

      CartItemEntity cartItem = new CartItemEntity();
      cartItem.setProduct(product);
      cartItem.setPrice(BigDecimal.TEN);
      cartItem.setQuantity(1);

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), eq(userId)))
          .thenReturn(List.of(cartItem));

      orderService.checkout(request);

      verify(orderItemRepository)
          .saveAll(
              argThat(
                  items ->
                      ((List<OrderItemEntity>) items).get(0).getProductImage().equals("img.jpg")));
    }

    @Test
    @DisplayName("checkout should fail if user not found")
    void checkout_UserNotFound() {
      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest()))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User not found");
    }

    @Test
    @DisplayName("checkout should fail if no items selected")
    void checkout_NoItems() {
      CheckoutRequest request = new CheckoutRequest();
      request.setCartItemIds(List.of(1L));

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), eq(userId)))
          .thenReturn(Collections.emptyList());

      assertThatThrownBy(() -> orderService.checkout(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("No items selected");
    }

    @Test
    @DisplayName("checkout should fail if item count mismatch")
    void checkout_ItemCountMismatch() {
      CheckoutRequest request = new CheckoutRequest();
      request.setCartItemIds(List.of(1L, 2L));

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(cartItemRepository.findByIdInAndCartCustomerId(anyList(), eq(userId)))
          .thenReturn(List.of(new CartItemEntity()));

      assertThatThrownBy(() -> orderService.checkout(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Some items are invalid or not yours");
    }
  }

  @Nested
  @DisplayName("handleWebhook tests")
  class WebhookTests {
    @Test
    @DisplayName("handleWebhook should detect duplicate event")
    void handleWebhook_DuplicateEvent() {
      Event event = mock(Event.class);
      when(event.getId()).thenReturn("evt_123");

      PaymentEntity existing = new PaymentEntity();
      OrderEntity order = new OrderEntity();
      order.setId(100L);
      existing.setOrder(order);

      when(paymentRepository.findByStripeEventId("evt_123")).thenReturn(Optional.of(existing));

      assertThatThrownBy(() -> orderService.handleWebhook(event, "payload"))
          .isInstanceOf(DuplicateWebhookEventException.class);
    }

    @Test
    @DisplayName("handleWebhook should handle parse failure")
    void handleWebhook_ParseFailure() throws Exception {
      Event event = mock(Event.class);
      when(event.getId()).thenReturn("evt_123");
      when(paymentRepository.findByStripeEventId(any())).thenReturn(Optional.empty());
      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class)))
          .thenThrow(mock(com.fasterxml.jackson.core.JsonProcessingException.class));

      assertThatThrownBy(() -> orderService.handleWebhook(event, "invalid"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Failed to parse webhook payload");
    }

    @Test
    @DisplayName("handleWebhook should fail if verification fails")
    void handleWebhook_VerificationFailure() throws Exception {
      Event event = mock(Event.class);
      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class)))
          .thenReturn(new WebhookPayload());
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(false);

      assertThatThrownBy(() -> orderService.handleWebhook(event, "payload"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Webhook payload missing transactionId");
    }

    @Test
    @DisplayName("handleWebhook should fail if payment not found")
    void handleWebhook_PaymentNotFound() throws Exception {
      Event event = mock(Event.class);
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");

      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.handleWebhook(event, "payload"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Payment not found");
    }

    @Test
    @DisplayName("handleWebhook should skip if order not pending")
    void handleWebhook_NotPending() throws Exception {
      Event event = mock(Event.class);
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");

      OrderEntity order = new OrderEntity();
      order.setStatus(OrderStatus.CONFIRMED);
      PaymentEntity payment = new PaymentEntity();
      payment.setOrder(order);

      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      orderService.handleWebhook(event, "payload");

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleWebhook should handle success")
    void handleWebhook_Success() throws Exception {
      Event event = mock(Event.class);
      when(event.getType()).thenReturn("checkout.session.completed");
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");

      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.PENDING);
      order.setUser(user);

      PaymentEntity payment = new PaymentEntity();
      payment.setOrder(order);

      OrderItemEntity item = new OrderItemEntity();
      ProductEntity p = new ProductEntity();
      p.setId(50L);
      item.setProduct(p);

      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));
      when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));

      orderService.handleWebhook(event, "payload");

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      verify(inventoryService).deductStock(anyList());
      verify(cartItemRepository).deleteByCartCustomerIdAndProductIdIn(eq(userId), anyList());
    }

    @Test
    @DisplayName("handleWebhook should handle expired")
    void handleWebhook_Expired() throws Exception {
      Event event = mock(Event.class);
      when(event.getType()).thenReturn("checkout.session.expired");
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");

      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.PENDING);
      PaymentEntity payment = new PaymentEntity();
      payment.setOrder(order);

      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      orderService.handleWebhook(event, "payload");

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      verify(inventoryService).releaseStock(anyList());
    }

    @Test
    @DisplayName("handleWebhook should handle failed")
    void handleWebhook_Failed() throws Exception {
      Event event = mock(Event.class);
      when(event.getType()).thenReturn("payment_intent.payment_failed");
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");

      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.PENDING);
      PaymentEntity payment = new PaymentEntity();
      payment.setOrder(order);

      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      orderService.handleWebhook(event, "payload");

      assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("handleWebhook should ignore unknown types")
    void handleWebhook_Unknown() throws Exception {
      Event event = mock(Event.class);
      when(event.getType()).thenReturn("unknown");
      WebhookPayload payload = new WebhookPayload();
      payload.setTransactionId("tx_123");

      OrderEntity order = new OrderEntity();
      order.setStatus(OrderStatus.PENDING);
      PaymentEntity payment = new PaymentEntity();
      payment.setOrder(order);

      when(objectMapper.readValue(anyString(), eq(WebhookPayload.class))).thenReturn(payload);
      when(paymentGatewayService.verifyTransaction(any())).thenReturn(true);
      when(paymentRepository.findByTransactionId("tx_123")).thenReturn(Optional.of(payment));

      orderService.handleWebhook(event, "payload");

      verify(orderRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getOrderStatus tests")
  class GetStatusTests {
    @Test
    void getOrderStatus_Works() {
      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.PENDING);
      PaymentEntity payment = new PaymentEntity();
      payment.setStatus(PaymentStatus.PENDING);

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(orderRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(order));
      when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

      OrderStatusResponse response = orderService.getOrderStatus(100L);
      assertThat(response.getOrderId()).isEqualTo(100L);
    }

    @Test
    void getOrderStatus_OrderNotFound() {
      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.getOrderStatus(100L))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getOrderStatus_PaymentNotFound() {
      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(orderRepository.findByIdAndUserId(anyLong(), anyLong()))
          .thenReturn(Optional.of(new OrderEntity()));
      when(paymentRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.getOrderStatus(100L))
          .isInstanceOf(RuntimeException.class);
    }
  }

  @Test
  @DisplayName("getOrders should return list")
  void getOrders_Works() {
    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(orderRepository.findByUserId(userId)).thenReturn(List.of(new OrderEntity()));

    List<OrderDetailResponse> response = orderService.getOrders();
    assertThat(response).hasSize(1);
  }

  @Nested
  @DisplayName("paginated tests")
  class PaginationTests {
    @Test
    void getMyOrdersPaginated_WithStatus() {
      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
              eq(userId), eq(OrderStatus.PENDING), any()))
          .thenReturn(new PageImpl<>(List.of(new OrderEntity())));

      Pagination<OrderDetailResponse> response =
          orderService.getMyOrdersPaginated(0, 10, OrderStatus.PENDING);
      assertThat(response.getData()).hasSize(1);
    }

    @Test
    void getMyOrdersPaginated_NoStatus() {
      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any()))
          .thenReturn(new PageImpl<>(List.of(new OrderEntity())));

      Pagination<OrderDetailResponse> response = orderService.getMyOrdersPaginated(0, 10, null);
      assertThat(response.getData()).hasSize(1);
    }

    @Test
    void getAllOrdersPaginatedForAdmin_WithStatus() {
      when(orderRepository.findByStatusOrderByCreatedAtDesc(eq(OrderStatus.PENDING), any()))
          .thenReturn(new PageImpl<>(List.of(new OrderEntity())));

      Pagination<OrderDetailResponse> response =
          orderService.getAllOrdersPaginatedForAdmin(0, 10, OrderStatus.PENDING);
      assertThat(response.getData()).hasSize(1);
    }

    @Test
    void getAllOrdersPaginatedForAdmin_NoStatus() {
      when(orderRepository.findAllByOrderByCreatedAtDesc(any()))
          .thenReturn(new PageImpl<>(List.of(new OrderEntity())));

      Pagination<OrderDetailResponse> response =
          orderService.getAllOrdersPaginatedForAdmin(0, 10, null);
      assertThat(response.getData()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("getOrderDetail tests")
  class DetailTests {
    @Test
    void getOrderDetail_Admin() {
      user.setRole(UserRole.ADMIN);
      OrderEntity order = new OrderEntity();
      order.setId(100L);

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.getOrderDetail(100L);
      verify(orderRepository).findById(100L);
    }

    @Test
    void getOrderDetail_Customer() {
      OrderEntity order = new OrderEntity();
      order.setId(100L);

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(order));

      orderService.getOrderDetail(100L);
      verify(orderRepository).findByIdAndUserId(100L, userId);
    }

    @Test
    void getOrderDetail_NotFound() {
      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.getOrderDetail(100L))
          .isInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  @DisplayName("cancelOrder tests")
  class CancelTests {
    @Test
    void cancelOrder_Works_Refund() {
      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.CONFIRMED);

      PaymentEntity payment = new PaymentEntity();
      payment.setStatus(PaymentStatus.SUCCESS);
      payment.setPaymentMethod(PaymentMethod.STRIPE);
      payment.setTransactionId("tx_123");

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(order));
      when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

      orderService.cancelOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
      verify(paymentGatewayService).refund("tx_123");
    }

    @Test
    void cancelOrder_InvalidStatus() {
      OrderEntity order = new OrderEntity();
      order.setStatus(OrderStatus.SHIPPING);

      when(userHelper.getCurrentUserId()).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> orderService.cancelOrder(100L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("cannot be cancelled");
    }
  }

  @Nested
  @DisplayName("reconcileSingleOrder tests")
  class ReconcileTests {
    @Test
    void reconcile_Complete() {
      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.PENDING);

      PaymentEntity payment = new PaymentEntity();
      payment.setTransactionId("sess_123");
      order.setPayment(payment);

      Session session = mock(Session.class);
      when(session.getStatus()).thenReturn("complete");
      when(stripeAdapter.retrieveSession("sess_123")).thenReturn(session);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void reconcile_Expired() {
      OrderEntity order = new OrderEntity();
      order.setId(100L);
      order.setStatus(OrderStatus.PENDING);
      order.setPayment(new PaymentEntity());
      order.getPayment().setTransactionId("sess_123");

      Session session = mock(Session.class);
      when(session.getStatus()).thenReturn("expired");
      when(stripeAdapter.retrieveSession("sess_123")).thenReturn(session);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void reconcile_SkipNonPending() {
      OrderEntity order = new OrderEntity();
      order.setStatus(OrderStatus.CONFIRMED);
      when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

      orderService.reconcileSingleOrder(100L);
      verify(stripeAdapter, never()).retrieveSession(any());
    }
  }
}
