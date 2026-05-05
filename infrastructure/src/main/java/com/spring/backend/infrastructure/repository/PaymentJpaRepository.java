package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
  Optional<PaymentEntity> findByOrderId(Long orderId);

  Optional<PaymentEntity> findByTransactionId(String transactionId);

  Optional<PaymentEntity> findByStripeEventId(String stripeEventId);
}
