package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.CategoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  private final UserHelper userHelper;

  @Override
  public CategoryResponseDto createCategory(CategoryRequestDto dto) {

    CategoryEntity entity =
        CategoryEntity.builder()
            .name(dto.getName())
            .note(dto.getNote())
            .isActive(true)
            .createdAt(Instant.now())
            .createdBy(1L)
            .build();

    categoryRepository.save(entity);

    return toResponseDto(entity);
  }

  @Override
  public List<CategoryResponseDto> getAllCategories() {
    return categoryRepository.findAll().stream()
        .map(this::toResponseDto)
        .collect(Collectors.toList());
  }

  private CategoryResponseDto toResponseDto(CategoryEntity entity) {
    return CategoryResponseDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .note(entity.getNote())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .createdBy(1L)
        .updatedAt(entity.getUpdatedAt())
        .updatedBy(1L)
        .build();
  }
}
