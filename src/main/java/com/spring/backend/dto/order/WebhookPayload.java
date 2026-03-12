package com.spring.backend.dto.order;

import lombok.Data;

@Data
public class WebhookPayload {
  private String transactionId;
  private boolean success;
  private String rawResponse;
}
