package com.spring.backend.dto.product;

import com.spring.backend.entity.BaseEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.enums.ProductStatus;
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
  private Double price;
  private Instant startDay;
  private Instant endDate;
  private String type;
  private String description;
  private Integer quantity;
  private ProductStatus status;
  private Long categoryId;
  private Long customerId;

  private List<Long> imageIds;

  public ProductRequestDto(ProductEntity productEntity) {
    this.id = productEntity.getId();
    this.name = productEntity.getName();
    this.code = productEntity.getCode();
    this.price = productEntity.getPrice();
    this.startDay = productEntity.getStartDay();
    this.endDate = productEntity.getEndDate();
    this.type = productEntity.getType();
    this.description = productEntity.getDescription();
    this.quantity = productEntity.getQuantity();
    this.status = productEntity.getStatus();
    this.categoryId = productEntity.getCategoryId();
    this.customerId = productEntity.getCustomerId();
    this.imageIds = productEntity.getImages().stream().map(BaseEntity::getId).toList();
  }
}
