package com.spring.backend.job;

import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.repository.OrderItemRepository;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.repository.PaymentRepository;
import com.spring.backend.service.InventoryService;
import com.stripe.model.checkout.Session;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reconciliation job that periodically checks PENDING orders whose Stripe sessions may have
 * resolved outside the webhook path (e.g. missed webhook delivery). Runs every 15 minutes.
 *
 * <p>Each order is processed in its own REQUIRES_NEW transaction so that a failure on one order
 * does not roll back the entire batch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationJobService {

  /**
   * Orders stuck in PENDING for longer than this duration are considered reconciliation candidates.
   */
  private static final int STUCK_THRESHOLD_MINUTES = 5;

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final PaymentRepository paymentRepository;
  private final StripeAdapter stripeAdapter;
  private final InventoryService inventoryService;

  @Recurring(id = "reconcile-stuck-pending-orders", cron = "0 */15 * * * *")
  @Job(name = "reconcile-stuck-pending-orders", retries = 0)
  @Transactional(readOnly = true)
  public void reconcileStuckOrders() {
    Instant cutoff = Instant.now().minus(STUCK_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
    List<OrderEntity> stuckOrders = orderRepository.findStuckPendingOrders(cutoff);

    if (stuckOrders.isEmpty()) {
      log.debug("Reconciliation: no stuck orders found");
      return;
    }

    log.info("Reconciliation: found {} stuck PENDING order(s) to check", stuckOrders.size());
    for (OrderEntity order : stuckOrders) {
      try {
        processSingleOrder(order.getId());
      } catch (Exception e) {
        log.error("Reconciliation failed for order {}: {}", order.getId(), e.getMessage(), e);
        // Continue with next order — isolation via REQUIRES_NEW
      }
    }
  }

  /**
   * Processes a single order in its own transaction so a failure is isolated. Re-fetches the order
   * inside the new transaction to get a fresh state.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processSingleOrder(Long orderId) {
    OrderEntity order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new RuntimeException("Order not found during reconciliation: " + orderId));

    // Guard: re-check status inside the new transaction to avoid race conditions
    if (order.getStatus() != OrderStatus.PENDING) {
      log.debug(
          "Reconciliation: order {} already in status {}, skipping", orderId, order.getStatus());
      return;
    }

    PaymentEntity payment = order.getPayment();
    if (payment == null || payment.getTransactionId() == null) {
      log.warn("Reconciliation: order {} has no payment/transactionId, skipping", orderId);
      return;
    }

    String sessionId = payment.getTransactionId();
    log.info("Reconciliation: checking Stripe session {} for order {}", sessionId, orderId);

    Session session = stripeAdapter.retrieveSession(sessionId);
    String status = session.getStatus(); // "complete" | "expired" | "open"

    switch (status) {
      case "complete" -> {
        log.info(
            "Reconciliation: session {} is complete — marking order {} CONFIRMED",
            sessionId,
            orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(Instant.now());

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        inventoryService.deductStock(items);

        orderRepository.save(order);
        paymentRepository.save(payment);
      }
      case "expired" -> {
        log.warn("Reconciliation: session {} expired — cancelling order {}", sessionId, orderId);
        order.setStatus(OrderStatus.CANCELLED);
        payment.setStatus(PaymentStatus.FAILED);

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        inventoryService.releaseStock(items);

        orderRepository.save(order);
        paymentRepository.save(payment);
      }
      default ->
          log.info(
              "Reconciliation: session {} status={} for order {} — no action taken",
              sessionId,
              status,
              orderId);
    }
  }
}
