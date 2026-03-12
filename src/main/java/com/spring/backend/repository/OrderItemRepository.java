package com.spring.backend.repository;

import com.spring.backend.entity.OrderItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
  List<OrderItemEntity> findByOrderId(Long orderId);
}
