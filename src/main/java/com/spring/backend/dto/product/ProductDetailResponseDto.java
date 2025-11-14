package com.spring.backend.dto.product;

import com.spring.backend.dto.image.ImageResponseDto;
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
  private Double price;
  private Instant startDay;
  private Instant endDate;
  private String type;
  private String code;
  private List<ImageResponseDto> images;

  public ProductDetailResponseDto(ProductEntity productEntity) {
    this.id = productEntity.getId();
    this.name = productEntity.getName();
    this.price = productEntity.getPrice();
    this.startDay = productEntity.getStartDay();
    this.endDate = productEntity.getEndDate();
    this.type = productEntity.getType();
    this.code = productEntity.getCode();
    this.images =
        productEntity.getImages().stream()
            .map(i -> ImageResponseDto.builder().id(i.getId()).url(i.getFileName()).build())
            .toList();
  }
}
