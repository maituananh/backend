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
public class OICChatResponseDto extends ChatResponseDto {
  private String idNumber;
  private String fullName;
  private String dateOfBirth;
  private String gender;
  private String nationality;
  private String placeOfOrigin;
  private String placeOfResidence;
  private String expiryDate;
}
