package com.spring.backend.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
  private String eventType;
  private String transactionId;
  private String idempotencyKey;
  private String status;

  @JsonProperty("type")
  public void setEventType(String type) {
    this.eventType = type;
  }

  @JsonProperty("request")
  public void unpackRequest(Map<String, Object> request) {
    if (request != null) {
      this.idempotencyKey = (String) request.get("idempotency_key");
    }
  }

  @JsonProperty("data")
  public void unpackData(Map<String, Object> data) {
    if (data != null && data.get("object") instanceof Map) {
      Map<String, Object> object = (Map<String, Object>) data.get("object");

      this.status = (String) object.get("status");
      this.transactionId = (String) object.get("id");
    }
  }
}
