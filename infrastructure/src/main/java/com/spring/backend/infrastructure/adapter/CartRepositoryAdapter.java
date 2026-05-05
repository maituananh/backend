package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.cart.Cart;
import com.spring.backend.domain.cart.CartRepository;
import com.spring.backend.infrastructure.mapper.CartMapper;
import com.spring.backend.infrastructure.repository.CartJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository jpaRepository;
    private final CartMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findByCustomerId(Long customerId) {
        // @Transactional required: CartEntity.items is FetchType.LAZY.
        // mapper.toDomain() calls entity.getItems() — must be inside active session.
        return jpaRepository.findByCustomerId(customerId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Cart save(Cart cart) {
        // INTENTIONALLY UNIMPLEMENTED: CartService.addToCart() routes cart saves through
        // cartJpaRepository (entity-level) — see Plan 09-05 for routing details.
        // CartRepositoryAdapter.save() is NEVER called in the current production flow.
        // This stub satisfies the domain interface contract only.
        // WARNING: Any future caller that invokes this method will receive a runtime exception.
        throw new UnsupportedOperationException(
            "CartRepositoryAdapter.save() not yet implemented — CartService writes route through cartJpaRepository — see Plan 09-05");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cart> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
