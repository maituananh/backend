package com.spring.backend.controller.category;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;

  @PostMapping
  public CategoryResponseDto createCategory(@RequestBody CategoryRequestDto request) {
    return categoryService.createCategory(request);
  }

  @GetMapping
  public List<CategoryResponseDto> getAllCategories() {
    return categoryService.getAllCategories();
  }

  @GetMapping("/search")
  public Page<CategoryResponseDto> searchCategories(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return categoryService.searchByName(name, page, size);
  }

  @GetMapping("/{id}")
  public CategoryResponseDto getCategoryById(@PathVariable Long id) {
    return categoryService.getCategoryById(id);
  }

  @PutMapping("/{id}")
  public CategoryResponseDto updateCategory(
      @PathVariable Long id, @RequestBody CategoryRequestDto request) {
    return categoryService.updateCategory(id, request);
  }

  @DeleteMapping("/{id}")
  public void deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
  }
}
