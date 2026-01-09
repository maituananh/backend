package com.spring.backend.repository;

import com.spring.backend.entity.CartItemEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

  Optional<CartItemEntity> findByCartIdAndProductId(Long cartId, Long productId);
}
