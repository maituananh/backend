package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.payment.Payment;
import com.spring.backend.domain.payment.PaymentRepository;
import com.spring.backend.infrastructure.mapper.PaymentMapper;
import com.spring.backend.infrastructure.repository.PaymentJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByStripeEventId(String stripeEventId) {
        return jpaRepository.findByStripeEventId(stripeEventId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        var entity = mapper.toEntity(payment);
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
