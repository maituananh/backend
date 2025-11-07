package com.spring.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "categories")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CategoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String note;

  @Column(name = "is_active")
  private Boolean isActive;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "updated_by")
  private Long updatedBy;
}
