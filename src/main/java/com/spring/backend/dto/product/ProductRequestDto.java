package com.spring.backend.dto.product;

import com.spring.backend.entity.ProductEntity;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
  private Long id;

  private String name;
  private String code;
  private String type;
  private Double price;
  private Double dailyProfit;
  private Integer quantity;
  private Instant startedAt;
  private Instant endAt;
  private Long categoryId;
  private String description;
  private List<Long> imageIds;
  private Long customerId;

  public ProductRequestDto(ProductEntity productEntity) {
    this.id = productEntity.getId();
    this.name = productEntity.getName();
    this.code = productEntity.getCode();
    this.type = productEntity.getType();
    this.price = productEntity.getPrice();
    this.dailyProfit = productEntity.getDailyProfit();
    this.quantity = productEntity.getQuantity();
    this.startedAt = productEntity.getStartedAt();
    this.endAt = productEntity.getEndAt();
    this.categoryId = productEntity.getCategoryId();
    this.description = productEntity.getDescription();
    this.customerId = productEntity.getCustomerId();
  }
}
