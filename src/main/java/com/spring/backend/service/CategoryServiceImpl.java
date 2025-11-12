package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  public CategoryResponseDto createCategory(CategoryRequestDto dto) {

    CategoryEntity entity =
        CategoryEntity.builder().name(dto.getName()).note(dto.getNote()).isActive(true).build();

    categoryRepository.save(entity);

    return new CategoryResponseDto(entity);
  }

  @Override
  public List<CategoryResponseDto> getAllCategories() {
    return categoryRepository.findByIsActiveIsTrue().stream()
        .map(CategoryResponseDto::new)
        .collect(Collectors.toList());
  }

  @Override
  public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
    CategoryEntity entity =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    entity.setName(dto.getName());
    entity.setNote(dto.getNote());

    categoryRepository.save(entity);
    return new CategoryResponseDto(entity);
  }

  @Override
  public void deleteCategory(Long id) {
    CategoryEntity entity =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    entity.setIsActive(false);
    categoryRepository.save(entity);
  }
}
