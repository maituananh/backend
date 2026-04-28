package com.spring.backend.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.spring.backend.entity.OrderEntity;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.job.ReconciliationJobService;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.service.OrderService;
import java.time.Instant;
import java.util.List;
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
  @Mock private OrderService orderService;
  @InjectMocks private ReconciliationJobService reconciliationJobService;

  private OrderEntity order1;
  private OrderEntity order2;

  @BeforeEach
  void setUp() {
    order1 = OrderEntity.builder().id(1L).status(OrderStatus.PENDING).build();
    order2 = OrderEntity.builder().id(2L).status(OrderStatus.PENDING).build();
  }

  @Nested
  @DisplayName("reconcileStuckOrders")
  class ReconcileStuckOrdersTests {

    @Test
    @DisplayName("no stuck orders — skips without calling OrderService")
    void noStuckOrders_doesNothing() {
      when(orderRepository.findStuckPendingOrders(any(Instant.class), any(Instant.class)))
          .thenReturn(List.of());

      reconciliationJobService.reconcileStuckOrders();

      verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("single stuck order — calls reconcileSingleOrder once")
    void singleStuckOrder_callsReconcileOnce() {
      when(orderRepository.findStuckPendingOrders(any(Instant.class), any(Instant.class)))
          .thenReturn(List.of(order1));

      reconciliationJobService.reconcileStuckOrders();

      verify(orderService).reconcileSingleOrder(1L);
      verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("multiple stuck orders — calls reconcileSingleOrder for each")
    void multipleStuckOrders_callsReconcileForEach() {
      when(orderRepository.findStuckPendingOrders(any(Instant.class), any(Instant.class)))
          .thenReturn(List.of(order1, order2));

      reconciliationJobService.reconcileStuckOrders();

      verify(orderService).reconcileSingleOrder(1L);
      verify(orderService).reconcileSingleOrder(2L);
    }

    @Test
    @DisplayName("one order throws — continues processing remaining orders (fault isolation)")
    void oneOrderThrows_continuesWithNext() {
      when(orderRepository.findStuckPendingOrders(any(Instant.class), any(Instant.class)))
          .thenReturn(List.of(order1, order2));
      doThrow(new RuntimeException("Stripe timeout")).when(orderService).reconcileSingleOrder(1L);

      reconciliationJobService.reconcileStuckOrders();

      verify(orderService).reconcileSingleOrder(1L);
      verify(orderService).reconcileSingleOrder(2L);
    }

    @Test
    @DisplayName("all orders throw — does not propagate exception (fault isolation)")
    void allOrdersThrow_doesNotPropagateException() {
      when(orderRepository.findStuckPendingOrders(any(Instant.class), any(Instant.class)))
          .thenReturn(List.of(order1, order2));
      doThrow(new RuntimeException("Stripe unavailable"))
          .when(orderService)
          .reconcileSingleOrder(anyLong());

      reconciliationJobService.reconcileStuckOrders();

      verify(orderService, times(2)).reconcileSingleOrder(anyLong());
    }
  }
}
