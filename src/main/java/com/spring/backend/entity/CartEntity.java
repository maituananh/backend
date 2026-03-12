package com.spring.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "carts",
    uniqueConstraints = @UniqueConstraint(columnNames = "customer_id"),
    indexes = @Index(name = "idx_cart_customer", columnList = "customer_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"customer", "items"})
@EqualsAndHashCode(callSuper = true)
public class CartEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private UserEntity customer;

  @OneToMany(
      mappedBy = "cart",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<CartItemEntity> items = new ArrayList<>();

  public BigDecimal getTotalAmount(List<CartItemEntity> cartItemEntities) {
    return cartItemEntities.stream()
        .map(CartItemEntity::getTotalItem)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public String getCartName(List<CartItemEntity> cartItemEntities) {
    return cartItemEntities.stream()
        .map(i -> i.getProduct().getName())
        .collect(Collectors.joining(", "));
  }
}
