package com.spring.backend.service;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface CategoryService {
  CategoryResponseDto createCategory(CategoryRequestDto dto);

  List<CategoryResponseDto> getAllCategories();

  CategoryResponseDto getCategoryById(Long id);

  Page<CategoryResponseDto> searchByName(String name, int page, int size);

  CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);

  void deleteCategory(Long id);
}
