package com.spring.backend.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponseDto {
  private String status;
  private String message;
  private String sessionId;
  private String sessionUrl;
}
