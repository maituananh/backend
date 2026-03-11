package com.spring.backend.dto.checkout;

import com.spring.backend.enums.OrderStatus;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
  private Long orderId;
  private String paymentUrl; // redirect sang gateway
  private BigDecimal totalAmount;
  private OrderStatus status;
}
