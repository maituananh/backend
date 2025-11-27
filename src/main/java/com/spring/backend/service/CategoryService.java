package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
  CategoryResponseDto createCategory(CategoryRequestDto dto);

  CategoryResponseDto getCategoryByName(String name);

  List<CategoryResponseDto> getAllCategories();

  CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);

  void deleteCategory(Long id);
}
