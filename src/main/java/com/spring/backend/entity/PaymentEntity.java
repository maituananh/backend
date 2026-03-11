package com.spring.backend.entity;

import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "payments",
    indexes = {
      @Index(name = "idx_payment_order", columnList = "order_id"),
      @Index(name = "idx_payment_transaction", columnList = "transaction_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"order"})
@EqualsAndHashCode(callSuper = true)
public class PaymentEntity extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private OrderEntity order;

  @Column(name = "transaction_id", unique = true)
  private String transactionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name = "gateway_response", columnDefinition = "TEXT")
  private String gatewayResponse;

  @Column(name = "paid_at")
  private Instant paidAt;
}
