package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
  CategoryResponseDto createCategory(CategoryRequestDto dto, String createdBy);

  List<CategoryResponseDto> getAllCategories();
}
