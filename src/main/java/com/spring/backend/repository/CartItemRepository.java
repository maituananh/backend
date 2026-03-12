package com.spring.backend.repository;

import com.spring.backend.entity.CartItemEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

  Optional<CartItemEntity> findByCartIdAndProductId(Long cartId, Long productId);

  @Query(
      "SELECT ci FROM CartItemEntity ci WHERE ci.id IN :ids AND ci.cart.customer.id = :customerId")
  List<CartItemEntity> findByIdInAndCartCustomerId(
      @Param("ids") List<Long> ids, @Param("customerId") Long customerId);

  @Modifying
  @Query("DELETE FROM CartItemEntity ci WHERE ci.id IN :ids AND ci.cart.customer.id = :customerId")
  void deleteByIdInAndCartCustomerId(
      @Param("ids") List<Long> ids, @Param("customerId") Long customerId);

  @Modifying
  @Query(
      "DELETE FROM CartItemEntity ci WHERE ci.cart.customer.id = :customerId AND ci.product.id IN :productIds")
  void deleteByCartCustomerIdAndProductIdIn(
      @Param("customerId") Long customerId, @Param("productIds") List<Long> productIds);

  @Modifying
  @Query("UPDATE CartItemEntity ci SET ci.price = :price WHERE ci.product.id = :productId")
  void updatePriceByProductId(@Param("productId") Long productId, @Param("price") BigDecimal price);
}
