package com.spring.backend.entity;

import com.spring.backend.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "users")
@Entity
@Getter
@Setter
@SuperBuilder
public class UserEntity extends BaseEntity {
  public UserEntity() {}

  @Column(name = "username", unique = true, nullable = true, updatable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "name")
  private String name;

  @Column(name = "age")
  private Integer age;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "card_id", nullable = false)
  private String cardId;

  @Column(name = "phone", nullable = false)
  private String phone;

  @Column(name = "address")
  private String address;

  @Column(name = "gender")
  private String gender;

  @Column(name = "`role`", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "avatar")
  private String avatar;

  @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ProductEntity> products;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<OrderEntity> orders;
}
