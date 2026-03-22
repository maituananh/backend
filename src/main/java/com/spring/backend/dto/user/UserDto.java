package com.spring.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spring.backend.enums.UserRole;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
  private Long id;
  private String email;
  private String name;
  private String username;
  private Integer age;
  private String birthDate;
  private String phone;

  @JsonProperty("card_id")
  private String cardId;

  private String address;
  private String gender;
  private UserRole role;
  private String avatar;
}
