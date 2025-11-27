package com.spring.backend.controller.category;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.CategoryService;
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
  public Object getCategories(@RequestParam(required = false) String name) {

    if (name != null && !name.isEmpty()) {
      CategoryResponseDto dto = categoryService.getCategoryByName(name);
      return dto;
    }

    return categoryRepository.findAll().stream()
        .map(
            category -> {
              CategoryResponseDto dto = new CategoryResponseDto(category);

              userRepository
                  .findById(category.getCreatedBy())
                  .ifPresent(user -> dto.setCreatedByUser(user.getName()));

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
