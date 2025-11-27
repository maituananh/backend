package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;

public interface CategoryService {
  CategoryResponseDto createCategory(CategoryRequestDto dto);

  CategoryResponseDto getCategoryByName(String name);

  CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);

  void deleteCategory(Long id);
}
