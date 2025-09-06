package com.spring.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String username;

  @Column private String password;

  @Column(name = "name")
  private String name;

  @Column(name = "age")
  private int age;

  @Column(name = "email")
  private String email;

  @Column(name = "card_id")
  private String cardId;

  @Column(name = "phone")
  private String phone;
}
