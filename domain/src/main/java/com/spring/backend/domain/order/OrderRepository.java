package com.spring.backend.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    Order save(Order order);
    List<Order> findByUserId(Long userId);
    List<Order> findAll();
}
