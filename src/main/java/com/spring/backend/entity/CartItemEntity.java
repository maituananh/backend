package com.spring.backend.entity;

import com.spring.backend.enums.CartItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "card_items",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"card_id", "product_id"})},
    indexes = {
      @Index(name = "idx_card_item_card", columnList = "card_id"),
      @Index(name = "idx_card_item_product", columnList = "product_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"card", "product"})
@EqualsAndHashCode(callSuper = true)
public class CartItemEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", nullable = false)
  private CartEntity card;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(nullable = false)
  private Double price;

  @Column(nullable = false)
  private Integer quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CartItemStatus status;

  @PrePersist
  void prePersist() {
    if (status == null) {
      status = CartItemStatus.PENDING;
    }
    if (quantity == null || quantity <= 0) {
      quantity = 1;
    }
  }
}
