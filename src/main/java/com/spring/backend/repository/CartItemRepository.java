package com.spring.backend.repository;

import com.spring.backend.entity.CartItemEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

  Optional<CartItemEntity> findByCard_IdAndProduct_Id(Long cardId, Long productId);
}
