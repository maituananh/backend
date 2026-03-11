package com.spring.backend.dto.chat;

import com.spring.backend.service.chat.CategoryAI;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ChatResponseDto {
  private String result;
  private String type;

  public static ChatResponseDto errorAnswer() {
    return new ChatResponseDto(
        "Sorry your question isn't supported !!", String.valueOf(CategoryAI.ERROR));
  }

  public static ChatResponseDto errorAnswer(String message) {
    return new ChatResponseDto(
        """
            Sorry your question isn't supported !!
            Detail: %s
            """
            .formatted(message),
        String.valueOf(CategoryAI.ERROR));
  }
}
