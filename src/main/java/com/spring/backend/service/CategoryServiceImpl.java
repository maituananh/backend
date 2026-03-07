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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
  public CategoryResponseDto getCategoryById(Long id) {

    CategoryEntity category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));

    UserEntity creator = null;

    if (category.getCreatedBy() != null) {
      creator = userRepository.findById(category.getCreatedBy()).orElse(null);
    }

    return CategoryMapper.toCategoryDto(category, creator);
  }

  @Override
  public Page<CategoryResponseDto> searchByName(String name, int page, int size) {

    Specification<CategoryEntity> spec = CategoryRepository.search(name);

    Pageable pageable = PageRequest.of(page, size);

    Page<CategoryEntity> categories = categoryRepository.findAll(spec, pageable);

    List<CategoryResponseDto> dtos =
        categories.getContent().stream()
            .map(
                category -> {
                  UserEntity creator = null;
                  if (category.getCreatedBy() != null) {
                    creator = userRepository.findById(category.getCreatedBy()).orElse(null);
                  }
                  return CategoryMapper.toCategoryDto(category, creator);
                })
            .toList();

    return new PageImpl<>(dtos, pageable, categories.getTotalElements());
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

    UserEntity creator = null;
    if (entity.getCreatedBy() != null) {
      creator = userRepository.findById(entity.getCreatedBy()).orElse(null);
    }

    return CategoryMapper.toCategoryDto(entity, creator);
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
