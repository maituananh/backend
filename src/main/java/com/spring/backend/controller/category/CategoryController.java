package com.spring.backend.controller.category;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.CategoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  @PostMapping
  public CategoryResponseDto createCategory(@RequestBody CategoryRequestDto request) {
    return categoryService.createCategory(request);
  }

  @GetMapping
  public List<CategoryResponseDto> getAllCategories() {
    return categoryRepository.findAll().stream()
        .map(
            category -> {
              CategoryResponseDto dto = new CategoryResponseDto(category);
              if (category.getCreatedBy() != null) {
                userRepository
                    .findById(category.getCreatedBy())
                    .ifPresent(
                        user -> {
                          dto.setCreatedBy(user.getName());
                        });
              }
              return dto;
            })
        .collect(Collectors.toList());
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
