package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.card.Card;
import com.spring.backend.domain.card.CardRepository;
import com.spring.backend.infrastructure.mapper.CardMapper;
import com.spring.backend.infrastructure.repository.CardJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CardRepositoryAdapter implements CardRepository {

    private final CardJpaRepository jpaRepository;
    private final CardMapper mapper;

    @Override
    public Optional<Card> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Card> findByCustomerId(Long customerId) {
        // CardJpaRepository.findByCustomerId returns List<CardEntity> (a customer may have multiple cards).
        // Domain interface requires Optional<Card> — return the first card found, if any.
        return jpaRepository.findByCustomerId(customerId).stream()
            .findFirst()
            .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Card save(Card card) {
        var entity = mapper.toEntity(card);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Card> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
