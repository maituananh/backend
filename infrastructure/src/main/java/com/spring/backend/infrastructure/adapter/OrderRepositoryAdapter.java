package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.order.Order;
import com.spring.backend.domain.order.OrderRepository;
import com.spring.backend.infrastructure.mapper.OrderMapper;
import com.spring.backend.infrastructure.repository.OrderJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        // @Transactional required: OrderEntity.items is FetchType.LAZY.
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        // INTENTIONALLY UNIMPLEMENTED: OrderService.checkout() and handleWebhook() route order saves
        // through orderJpaRepository (entity-level) — see Plan 09-05 for routing details.
        // OrderRepositoryAdapter.save() is NEVER called in the current production flow.
        // This stub satisfies the domain interface contract only.
        // WARNING: Any future caller that invokes this method will receive a runtime exception.
        throw new UnsupportedOperationException(
            "OrderRepositoryAdapter.save() not yet implemented — OrderService writes route through orderJpaRepository — see Plan 09-05");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
