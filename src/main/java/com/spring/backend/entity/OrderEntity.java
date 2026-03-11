package com.spring.backend.entity;

import com.spring.backend.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "orders",
    indexes = {@Index(name = "idx_order_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"user", "items", "payment"})
@EqualsAndHashCode(callSuper = true)
public class OrderEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;

  @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "shipping_name", nullable = false)
  private String shippingName;

  @Column(name = "shipping_phone", nullable = false, length = 20)
  private String shippingPhone;

  @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
  private String shippingAddress;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrderItemEntity> items = new ArrayList<>();

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private PaymentEntity payment;
}
