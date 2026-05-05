package com.spring.backend.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByStripeEventId(String stripeEventId);
    Payment save(Payment payment);
}
