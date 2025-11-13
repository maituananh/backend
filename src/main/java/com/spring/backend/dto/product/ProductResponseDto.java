package com.spring.backend.dto.product;

import com.spring.backend.entity.ProductEntity;
import com.spring.backend.enums.ProductStatus;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
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
  private String image;

  public ProductResponseDto(ProductEntity productEntity) {
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
    this.image =
        productEntity.getImages() != null && !productEntity.getImages().isEmpty()
            ? productEntity.getImages().get(0).getFileName()
            : null;
  }
}
