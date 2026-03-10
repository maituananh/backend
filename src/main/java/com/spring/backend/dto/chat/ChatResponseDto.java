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

  public static ChatResponseDto errorAnswer() {
    return new ChatResponseDto("Sorry your question isn't supported !!");
  }
}
