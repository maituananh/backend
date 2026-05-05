package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.OrderItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {
  List<OrderItemEntity> findByOrderId(Long orderId);
}
