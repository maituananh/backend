package com.spring.backend.dto.payment;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
  private List<Long> productIds;
  private String currency;
}
