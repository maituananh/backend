package com.spring.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateAccountRequestDto extends ChatRequestDto {
  private String username;
  private String phone;
  private String email;

  public boolean validateCreateAccountRequestDto() {
    if (this.username == null || this.username.isEmpty()) {
      return false;
    }

    if (this.phone == null || this.phone.isEmpty()) {
      return false;
    }

    if (this.email == null || this.email.isEmpty()) {
      return false;
    }

    return true;
  }
}
