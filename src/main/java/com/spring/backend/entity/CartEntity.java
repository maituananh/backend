package com.spring.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "cards",
    uniqueConstraints = @UniqueConstraint(columnNames = "customer_id"),
    indexes = @Index(name = "idx_card_customer", columnList = "customer_id"))
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
      mappedBy = "card",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<CartItemEntity> items = new ArrayList<>();
}
