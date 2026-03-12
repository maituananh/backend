package com.spring.backend.dto.order;

import lombok.Data;

@Data
public class WebhookPayload {
  private String transactionId;
  private String eventType;
  private boolean success;
  private String rawResponse;
}
