package com.spring.backend.job;

import com.spring.backend.entity.OrderEntity;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.service.OrderService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.springframework.stereotype.Service;
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
  private final OrderService orderService;

  @Recurring(id = "reconcile-stuck-pending-orders", cron = "0 */15 * * * *")
  @Job(name = "reconcile-stuck-pending-orders", retries = 0)
  @Transactional(readOnly = true)
  public void reconcileStuckOrders() {
    Instant lookbackCutoff = Instant.now().minus(24, ChronoUnit.HOURS);
    Instant minAgeCutoff = Instant.now().minus(STUCK_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
    List<OrderEntity> stuckOrders =
        orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

    if (stuckOrders.isEmpty()) {
      log.debug("Reconciliation: no stuck orders found");
      return;
    }

    log.info("Reconciliation: found {} stuck PENDING order(s) to check", stuckOrders.size());
    for (OrderEntity order : stuckOrders) {
      try {
        orderService.reconcileSingleOrder(order.getId());
      } catch (Exception e) {
        log.warn("Reconciliation failed for order {}: {}", order.getId(), e.getMessage(), e);
        // Continue with next order — isolation via REQUIRES_NEW
      }
    }
  }
}
