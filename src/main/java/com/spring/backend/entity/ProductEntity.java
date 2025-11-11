package com.spring.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String code;
  private String type;
  private Double price;
  private Double dailyProfit;
  private Integer quantity;
  private Instant startedAt;
  private Instant endAt;
  private String description;

  private Long categoryId;
  private Long customerId;

  private Instant createdAt;
  private Long createdBy;
  private Instant updatedAt;
  private Long updatedBy;

  @ElementCollection
  @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
  @Column(name = "image_id")
  private List<Long> imageIds;
}
