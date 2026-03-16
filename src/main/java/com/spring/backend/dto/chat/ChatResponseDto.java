package com.spring.backend.dto.chat;

import com.spring.backend.dto.classifier.ConversationMessage;
import com.spring.backend.service.chat.AIIntent;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ChatResponseDto<T> {
  private String status;
  private ChatData<T> data;
  private ChatError error;
  private ChatMeta meta;

  public ChatResponseDto(String errorMessage, String type) {
    this.status = "error";
    this.error = ChatError.builder().code(type).message(errorMessage).build();
  }

  @Data
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChatData<T> {
    private String reply;
    private String intent;
    private int step;
    private int stepTotal;
    private boolean isComplete;
    private String collectingField;
    private T result;
    private List<ConversationMessage> messages;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChatError {
    private String code;
    private String message;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChatMeta {
    private String sessionId;
    private long responseTime;
  }

  public static ChatResponseDto<Object> errorAnswer() {
    return new ChatResponseDto<>(
        "Sorry your question isn't supported !!", String.valueOf(AIIntent.ERROR));
  }

  public static ChatResponseDto<Object> errorAnswer(String message) {
    return new ChatResponseDto<>(
        """
            Sorry your question isn't supported !!
            Detail: %s
            """
            .formatted(message),
        String.valueOf(AIIntent.ERROR));
  }
}
