package com.spring.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "tokens")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenEntity {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "access_token", unique = true)
  private String accessToken;

  @Column(name = "refresh_token", nullable = false, unique = true)
  private String refreshToken;
}
