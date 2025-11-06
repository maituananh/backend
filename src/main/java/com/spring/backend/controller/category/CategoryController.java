package com.spring.backend.controller.category;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;

  @PostMapping
  public CategoryResponseDto createCategory(@RequestBody CategoryRequestDto request) {
    return categoryService.createCategory(request, "admin");
  }

  @GetMapping
  public List<CategoryResponseDto> getAllCategories() {
    return categoryService.getAllCategories();
  }
}
