package com.spring.backend.dto.chat;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ProfileChatResponseDto extends ChatResponseDto {
  private String number;
  private String name;
  private String dateOfBirth;
  private String gender;
  private String nationality;
  private String placeOfOrigin;
  private String placeOfResidence;
  private String expiryDate;
}
