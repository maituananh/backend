package com.spring.backend.dto.product;

import com.spring.backend.entity.ProductEntity;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponseDto {
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
  private List<Long> imageIds;

  private Instant createdAt;
  private Long createdBy;
  private Instant updatedAt;
  private Long updatedBy;

  public ProductDetailResponseDto(ProductEntity productEntity) {
    this.id = productEntity.getId();
    this.name = productEntity.getName();
    this.code = productEntity.getCode();
    this.type = productEntity.getType();
    this.price = productEntity.getPrice();
    this.dailyProfit = productEntity.getDailyProfit();
    this.quantity = productEntity.getQuantity();
    this.startedAt = productEntity.getStartedAt();
    this.endAt = productEntity.getEndAt();
    this.description = productEntity.getDescription();
    this.customerId = productEntity.getCustomerId();
    this.categoryId = productEntity.getCategoryId();
    this.createdAt = productEntity.getCreatedAt();
    this.createdBy = productEntity.getCreatedBy();
    this.updatedAt = productEntity.getUpdatedAt();
    this.updatedBy = productEntity.getUpdatedBy();

    this.imageIds = productEntity.getImageIds();
  }
}
