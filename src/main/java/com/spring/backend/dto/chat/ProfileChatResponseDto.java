package com.spring.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ProfileChatResponseDto extends ChatResponseDto {
  private String username;
  private String name;
  private int age;
  private String email;
  private String cardId;
  private String phone;
  private String address;
  private String gender;
  private String avatar;
}
