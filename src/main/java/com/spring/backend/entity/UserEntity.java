package com.spring.backend.entity;

import com.spring.backend.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseEntity {

  @Column(name = "username", unique = true, nullable = false, updatable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "name")
  private String name;

  @Column(name = "age")
  private int age;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "card_id", nullable = false, updatable = false)
  private String cardId;

  @Column(name = "phone", nullable = false)
  private String phone;

  @Column(name = "role", nullable = false)
  private UserRole role;
}
