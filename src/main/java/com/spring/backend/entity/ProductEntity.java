package com.spring.backend.entity;

import com.spring.backend.enums.ProductStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "products")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity extends BaseEntity {

  @Column(name = "name")
  private String name;

  @Column(name = "price")
  private double price;

  @Column(name = "startDay")
  private Instant startDay;

  @Column(name = "endDate")
  private Instant endDate;

  @Column(name = "type")
  private String type;

  @Column(name = "description")
  private String description;

  @Column(name = "quantity")
  private int quantity;

  @Column(name = "status")
  private ProductStatus status;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<ImageEntity> images;
}
