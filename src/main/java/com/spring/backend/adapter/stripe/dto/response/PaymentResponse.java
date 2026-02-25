package com.spring.backend.adapter.stripe.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
  private String status;
  private String message;
  private String sessionId;
  private String sessionUrl;
}
