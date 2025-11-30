package com.spring.backend.dto.user;

import com.spring.backend.enums.UserRole;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
  private String email;
  private String name;
  private String username;
  private int age;
  private String phone;
  private String cardId;
  private String address;
  private String gender;
  private UserRole role;
}
