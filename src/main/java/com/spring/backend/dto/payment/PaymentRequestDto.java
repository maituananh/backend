package com.spring.backend.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
  private Long productId;
  private Integer quantity;
  private String currency;
}
