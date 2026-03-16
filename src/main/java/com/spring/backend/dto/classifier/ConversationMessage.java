package com.spring.backend.dto.classifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
  private String role; // "user" | "assistant"
  private String content;
  private String timestamp;
}
