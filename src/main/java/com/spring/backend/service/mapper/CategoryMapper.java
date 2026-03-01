package com.spring.backend.service.mapper;

import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.UserEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

  public static CategoryResponseDto toCategoryDto(CategoryEntity entity, UserEntity createdBy) {
    return CategoryResponseDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .note(entity.getNote())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .updatedBy(entity.getUpdatedBy())
        .createdByUser(createdBy == null ? null : UserMapper.toUserDto(createdBy))
        .build();
  }

  public static CategoryResponseDto toCategoryDto(CategoryEntity entity) {
    return CategoryResponseDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .note(entity.getNote())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .updatedBy(entity.getUpdatedBy())
        .build();
  }
}
