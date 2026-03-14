package com.spring.backend.repository;

import com.spring.backend.entity.OrderEntity;
import com.spring.backend.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
