package com.spring.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "images")
@Entity
@Getter
@Setter
@SuperBuilder
public class ImageEntity extends BaseEntity {
  public ImageEntity() {}

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "product_id")
  private ProductEntity product;

  @Column(name = "file_name")
  private String fileName;
}
