package com.spring.backend.dto.order;

import com.spring.backend.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDashboardResponse {
  private Long orderId;

  private String customerName;
  private String cardId;

  private BigDecimal totalAmount;
  private OrderStatus orderStatus;
  private Instant createdAt;
}
