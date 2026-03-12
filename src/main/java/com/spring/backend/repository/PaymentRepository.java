package com.spring.backend.repository;

import com.spring.backend.entity.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
  Optional<PaymentEntity> findByOrderId(Long orderId);

  Optional<PaymentEntity> findByTransactionId(String transactionId);
}
