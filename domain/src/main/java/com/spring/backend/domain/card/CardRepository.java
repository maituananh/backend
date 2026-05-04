package com.spring.backend.domain.card;

import java.util.List;
import java.util.Optional;

public interface CardRepository {
    Optional<Card> findById(Long id);
    Optional<Card> findByCustomerId(Long customerId);
    Card save(Card card);
    List<Card> findAll();
}
