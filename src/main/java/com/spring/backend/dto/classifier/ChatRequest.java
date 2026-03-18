package com.spring.backend.dto.classifier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {
  @NotBlank private String sessionId;

  @NotBlank
  @Size(max = 1000)
  private String message;

  private String timestamp;
}
