package com.spring.backend.service.mapper;

import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.entity.ProductEntity;
import java.util.List;

public class ProductMapper {
  public static ProductEntity toProductEntity(
      ProductRequestDto dto, List<ImageEntity> imageEntities) {
    ProductEntity product =
        ProductEntity.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .startedAt(dto.getStartedAt())
            .endAt(dto.getEndAt())
            .type(dto.getType())
            .build();

    // product.setImageIds(imageEntities);

    return product;
  }

  public static ProductResponseDto toProductResponse(ProductEntity entity, String image) {
    return ProductResponseDto.builder()
        .name(entity.getName())
        .price(entity.getPrice())
        .id(entity.getId())
        .type(entity.getType())
        .startDay(entity.getStartedAt())
        .endDate(entity.getEndAt())
        .type(entity.getType())
        //   .imageIds(image)
        .build();
  }

  public static ProductDetailResponseDto toProductDetailResponse(
      ProductEntity entity, List<ImageResponseDto> images) {
    return ProductDetailResponseDto.builder()
        .name(entity.getName())
        .price(entity.getPrice())
        .id(entity.getId())
        .type(entity.getType())
        .startedAt(entity.getStartedAt())
        .endAt(entity.getEndAt())
        .type(entity.getType())
        //  .imageIds(images)
        .build();
  }
}
