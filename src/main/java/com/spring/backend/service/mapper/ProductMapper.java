package com.spring.backend.service.mapper;

import com.spring.backend.adapter.s3.S3Adapter;
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
            .code(dto.getCode())
            .dailyProfit(dto.getDailyProfit())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .description(dto.getDescription())
            .stockQty(dto.getStockQty())
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
        .code(entity.getCode())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .image(image)
        .isActived(entity.getIsActived())
        .status(entity.getStatus())
        .availableQty(entity.getAvailableQty())
        .stockQty(entity.getStockQty())
        .dailyProfit(entity.getDailyProfit() != null ? entity.getDailyProfit() : 0)
        .build();
  }

  public static ProductDetailResponseDto toProductDetailResponse(
      ProductEntity entity, List<ImageResponseDto> images, S3Adapter s3Adapter) {
    return ProductDetailResponseDto.builder()
        .name(entity.getName())
        .price(entity.getPrice())
        .id(entity.getId())
        .code(entity.getCode())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .images(images)
        .stockQty(entity.getStockQty())
        .availableQty(entity.getAvailableQty())
        .description(entity.getDescription())
        .category(CategoryMapper.toCategoryDto(entity.getCategory()))
        .user(UserMapper.toUserDto(entity.getCustomer(), s3Adapter))
        .dailyProfit(entity.getDailyProfit())
        .isActived(entity.getIsActived())
        .status(entity.getStatus())
        .build();
  }
}
