package com.spring.backend.repository;

import com.spring.backend.entity.CartItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

  Optional<CartItemEntity> findByCartIdAndProductId(Long cartId, Long productId);

  List<CartItemEntity> findByIdInAndCartCustomerId(List<Long> ids, Long customerId);

  void deleteByIdInAndCartCustomerId(List<Long> ids, Long customerId);

  void deleteByCartCustomerIdAndProductIdIn(Long customerId, List<Long> productIds);

  List<CartItemEntity> findByIdInAndCustomerId(List<Long> cartIds, Long userId);


}
