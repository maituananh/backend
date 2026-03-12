package com.spring.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "order_items",
    indexes = {
      @Index(name = "idx_order_item_order", columnList = "order_id"),
      @Index(name = "idx_order_item_product", columnList = "product_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"order", "product"})
@EqualsAndHashCode(callSuper = true)
public class OrderItemEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "product_image", length = 500)
  private String productImage;

  @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
  private BigDecimal subtotal;

  /** Convenience method - delegates to FK */
  public Long getProductId() {
    return product != null ? product.getId() : null;
  }
}
