package com.spring.backend.repository;

import com.spring.backend.entity.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  List<OrderEntity> findByUserId(Long userId);

  Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);
}
