package com.spring.backend.repository;

import com.spring.backend.entity.CardItemEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardItemRepository extends JpaRepository<CardItemEntity, Long> {

  Optional<CardItemEntity> findByCard_IdAndProduct_Id(Long cardId, Long productId);
}
