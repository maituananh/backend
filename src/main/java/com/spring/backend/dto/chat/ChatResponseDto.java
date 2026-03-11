package com.spring.backend.dto.chat;

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

  public ChatResponseDto(String result) {
    this.result = result;
  }

  public static ChatResponseDto errorAnswer() {
    return new ChatResponseDto("Sorry your question isn't supported !!");
  }

  public static ChatResponseDto errorAnswer(String message) {
    return new ChatResponseDto(
        """
            Sorry your question isn't supported !!
            Detail: %s
            """
            .formatted(message));
  }
}
