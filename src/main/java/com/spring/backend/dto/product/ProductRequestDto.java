package com.spring.backend.dto.product;

import com.spring.backend.entity.BaseEntity;
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
  private Double price;
  private Instant startDay;
  private Instant endDate;
  private String type;
  private List<Long> imageIds;

  public ProductRequestDto(ProductEntity productEntity) {
    this.id = productEntity.getId();
    this.name = productEntity.getName();
    this.price = productEntity.getPrice();
    this.startDay = productEntity.getStartDay();
    this.endDate = productEntity.getEndDate();
    this.type = productEntity.getType();
    this.imageIds = productEntity.getImages().stream().map(BaseEntity::getId).toList();
  }
}
