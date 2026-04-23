package com.spring.backend.repository;

import com.spring.backend.entity.OrderEntity;
import com.spring.backend.enums.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  List<OrderEntity> findByUserId(Long userId);

  Page<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(
      Long userId, OrderStatus status, Pageable pageable);

  Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

  List<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  Page<OrderEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<OrderEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

  /**
   * Finds PENDING orders with a Stripe transaction ID that are older than the given cutoff time.
   * Uses JOIN FETCH on payment to avoid N+1 queries in the reconciliation job.
   */
  @Query(
      "SELECT DISTINCT o FROM OrderEntity o "
          + "JOIN FETCH o.payment p "
          + "WHERE o.status = 'PENDING' "
          + "AND p.transactionId IS NOT NULL "
          + "AND o.createdAt >= :lookbackCutoff "
          + "AND o.createdAt < :minAgeCutoff")
  List<OrderEntity> findStuckPendingOrders(
      @Param("lookbackCutoff") Instant lookbackCutoff,
      @Param("minAgeCutoff") Instant minAgeCutoff);
}
