package com.spring.backend.entity;

import com.spring.backend.enums.CartItemStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id", "product_id"})},
    indexes = {
      @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
      @Index(name = "idx_cart_item_product", columnList = "product_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"cart", "product"})
@EqualsAndHashCode(callSuper = true)
public class CartItemEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private CartEntity cart;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CartItemStatus status;

  public BigDecimal getTotalItem() {
    return getPrice().multiply(BigDecimal.valueOf(getQuantity()));
  }
}
