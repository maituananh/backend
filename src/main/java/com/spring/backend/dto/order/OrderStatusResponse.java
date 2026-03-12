package com.spring.backend.dto.order;

import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusResponse {
  private Long orderId;
  private OrderStatus orderStatus;
  private PaymentStatus paymentStatus;
}
