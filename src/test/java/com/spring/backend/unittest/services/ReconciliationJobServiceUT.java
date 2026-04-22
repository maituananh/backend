package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.job.ReconciliationJobService;
import com.spring.backend.repository.OrderItemRepository;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.repository.PaymentRepository;
import com.spring.backend.service.InventoryService;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.Instant;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ReconciliationJobService Unit Tests")
class ReconciliationJobServiceUT {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private StripeAdapter stripeAdapter;
  @Mock private InventoryService inventoryService;
  @InjectMocks private ReconciliationJobService reconciliationJobService;

  private OrderEntity order;
  private PaymentEntity payment;

  @BeforeEach
  void setUp() {
    order =
        OrderEntity.builder()
            .id(1L)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(100_000))
            .shippingName("Test")
            .shippingPhone("0900000000")
            .shippingAddress("Street")
            .build();

    payment =
        PaymentEntity.builder()
            .id(10L)
            .order(order)
            .status(PaymentStatus.PENDING)
            .amount(BigDecimal.valueOf(100_000))
            .transactionId("sess_reconcile_test")
            .build();

    order.setPayment(payment);
  }

  // -----------------------------------------------------------------------
  // reconcileStuckOrders()
  // -----------------------------------------------------------------------
  @Nested
  @DisplayName("reconcileStuckOrders")
  class ReconcileStuckOrdersTests {

    @Test
    @DisplayName("should do nothing when no stuck orders found")
    void noStuckOrders_doesNothing() {
      when(orderRepository.findStuckPendingOrders(any(Instant.class))).thenReturn(List.of());

      reconciliationJobService.reconcileStuckOrders();

      verifyNoInteractions(stripeAdapter, inventoryService, paymentRepository);
    }

    @Test
    @DisplayName("should call processSingleOrder for each stuck order")
    void stuckOrders_callsProcessForEach() {
      OrderEntity order2 = OrderEntity.builder().id(2L).status(OrderStatus.PENDING).build();
      when(orderRepository.findStuckPendingOrders(any(Instant.class)))
          .thenReturn(List.of(order, order2));

      // processSingleOrder re-fetches; return empty so it exits early
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(orderRepository.findById(2L)).thenReturn(Optional.empty());

      // order1 has PENDING but no payment → skips gracefully
      order.setPayment(null);

      reconciliationJobService.reconcileStuckOrders();

      verify(orderRepository, times(2)).findById(anyLong());
    }

    @Test
    @DisplayName("should continue processing remaining orders when one fails")
    void oneOrderFails_continuesWithNext() {
      OrderEntity order2 = OrderEntity.builder().id(2L).status(OrderStatus.PENDING).build();
      order2.setPayment(null); // will be skipped cleanly

      when(orderRepository.findStuckPendingOrders(any(Instant.class)))
          .thenReturn(List.of(order, order2));

      // First order: Stripe throws
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(stripeAdapter.retrieveSession("sess_reconcile_test"))
          .thenThrow(new RuntimeException("Stripe unavailable"));

      // Second order
      when(orderRepository.findById(2L)).thenReturn(Optional.of(order2));

      reconciliationJobService.reconcileStuckOrders();

      // Both attempts made — failure on first does not skip second
      verify(orderRepository).findById(1L);
      verify(orderRepository).findById(2L);
    }
  }

  // -----------------------------------------------------------------------
  // processSingleOrder()
  // -----------------------------------------------------------------------
  @Nested
  @DisplayName("processSingleOrder")
  class ProcessSingleOrderTests {

    @Test
    @DisplayName("should mark order CONFIRMED when session is complete")
    void sessionComplete_confirmsOrder() {
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      Session mockSession = mock(Session.class);
      when(mockSession.getStatus()).thenReturn("complete");
      when(stripeAdapter.retrieveSession("sess_reconcile_test")).thenReturn(mockSession);

      OrderItemEntity item = OrderItemEntity.builder().build();
      when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));

      reconciliationJobService.processSingleOrder(1L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      assertThat(payment.getPaidAt()).isNotNull();
      verify(inventoryService).deductStock(anyList());
      verify(orderRepository).save(order);
      verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("should mark order CANCELLED and release stock when session is expired")
    void sessionExpired_cancelsOrder() {
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      Session mockSession = mock(Session.class);
      when(mockSession.getStatus()).thenReturn("expired");
      when(stripeAdapter.retrieveSession("sess_reconcile_test")).thenReturn(mockSession);

      OrderItemEntity item = OrderItemEntity.builder().build();
      when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));

      reconciliationJobService.processSingleOrder(1L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
      verify(inventoryService).releaseStock(anyList());
      verify(orderRepository).save(order);
      verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("should take no action when session is still open")
    void sessionOpen_noAction() {
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      Session mockSession = mock(Session.class);
      when(mockSession.getStatus()).thenReturn("open");
      when(stripeAdapter.retrieveSession("sess_reconcile_test")).thenReturn(mockSession);

      reconciliationJobService.processSingleOrder(1L);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
      verify(inventoryService, never()).deductStock(anyList());
      verify(inventoryService, never()).releaseStock(anyList());
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("should skip already processed order (status != PENDING)")
    void orderAlreadyProcessed_skips() {
      order.setStatus(OrderStatus.CONFIRMED);
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      reconciliationJobService.processSingleOrder(1L);

      verifyNoInteractions(stripeAdapter, inventoryService, paymentRepository);
    }

    @Test
    @DisplayName("should skip when order has no payment")
    void noPayment_skips() {
      order.setPayment(null);
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      reconciliationJobService.processSingleOrder(1L);

      verifyNoInteractions(stripeAdapter, inventoryService, paymentRepository);
    }

    @Test
    @DisplayName("should skip when payment has no transactionId")
    void noTransactionId_skips() {
      payment.setTransactionId(null);
      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      reconciliationJobService.processSingleOrder(1L);

      verifyNoInteractions(stripeAdapter, inventoryService);
    }
  }
}
