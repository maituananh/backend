package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.CategoryMapper;
import java.util.List;
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

    return CategoryMapper.toCategoryDto(entity, null);
  }

  @Override
  public List<CategoryResponseDto> getAllCategories() {
    return categoryRepository.findAll().stream()
        .map(
            category -> {
              UserEntity userEntity = null;
              if (category.getCreatedBy() != null) {
                userEntity = userRepository.findById(category.getCreatedBy()).orElse(null);
              }

              return CategoryMapper.toCategoryDto(category, userEntity);
            })
        .toList();
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
    return CategoryMapper.toCategoryDto(entity, null);
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
