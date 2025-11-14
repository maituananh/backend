package com.spring.backend.service.mapper;

import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.ProductStatus;
import java.util.List;

public class ProductMapper {
  public static ProductEntity toProductEntity(
      ProductRequestDto dto,
      List<ImageEntity> imageEntities,
      CategoryEntity categoryEntity,
      UserEntity userEntity) {
    ProductEntity product =
        ProductEntity.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .type(dto.getType())
            .code(dto.getCode())
            .dailyProfit(dto.getDailyProfit())
            .startDay(dto.getStartedAt())
            .endDate(dto.getEndAt())
            .description(dto.getDescription())
            .quantity(dto.getQuantity())
            .category(categoryEntity)
            .customer(userEntity)
            .status(ProductStatus.NEW)
            .build();

    product.setImages(imageEntities);

    return product;
  }

  public static ProductResponseDto toProductResponse(ProductEntity entity, String image) {
    return ProductResponseDto.builder()
        .name(entity.getName())
        .price(entity.getPrice())
        .id(entity.getId())
        .type(entity.getType())
        .code(entity.getCode())
        .startDay(entity.getStartDay())
        .endDate(entity.getEndDate())
        .type(entity.getType())
        .image(image)
        .build();
  }

  public static ProductDetailResponseDto toProductDetailResponse(
      ProductEntity entity, List<ImageResponseDto> images) {
    return ProductDetailResponseDto.builder()
        .name(entity.getName())
        .price(entity.getPrice())
        .id(entity.getId())
        .type(entity.getType())
        .code(entity.getCode())
        .startDay(entity.getStartDay())
        .endDate(entity.getEndDate())
        .type(entity.getType())
        .images(images)
        .build();
  }
}
