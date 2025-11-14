package com.spring.backend.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "categories")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CategoryEntity extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(name = "note")
  private String note;

  @Column(name = "is_active")
  private Boolean isActive;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ProductEntity> products;
}
