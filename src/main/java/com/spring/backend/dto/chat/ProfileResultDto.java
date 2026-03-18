package com.spring.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResultDto {
  private String username;
  private String name;
  private Integer age;
  private String birthDate;
  private String email;
  private String cardId;
  private String phone;
  private String address;
  private String gender;
  private String avatar;
}
