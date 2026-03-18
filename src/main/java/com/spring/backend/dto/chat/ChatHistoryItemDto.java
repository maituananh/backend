package com.spring.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryItemDto {
  private String userMessage;
  private String fileUrl;
  private ChatResponseDto<?> aiResponse;
  private String timestamp;
}
