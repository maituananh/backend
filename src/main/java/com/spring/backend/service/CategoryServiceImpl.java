package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  @Override
  public CategoryResponseDto createCategory(CategoryRequestDto dto) {

    CategoryEntity entity =
        CategoryEntity.builder().name(dto.getName()).note(dto.getNote()).isActive(true).build();

    categoryRepository.save(entity);

    return new CategoryResponseDto(entity);
  }

  @Override
  public CategoryResponseDto getCategoryByName(String name) {
    CategoryEntity entity =
        categoryRepository
            .findByNameIgnoreCase(name)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    CategoryResponseDto dto = new CategoryResponseDto(entity);

    userRepository
        .findById(entity.getCreatedBy())
        .ifPresent(user -> dto.setCreatedByUser(user.getName()));

    return dto;
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
