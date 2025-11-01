package com.spring.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "images")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(name = "file_name")
  private String fileName;
}
