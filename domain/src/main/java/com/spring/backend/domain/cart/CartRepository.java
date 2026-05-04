package com.spring.backend.domain.cart;

import java.util.List;
import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findById(Long id);
    Optional<Cart> findByCustomerId(Long customerId);
    Cart save(Cart cart);
    List<Cart> findAll();
}
