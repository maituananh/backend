package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.infrastructure.entity.CategoryEntity;
import com.spring.backend.infrastructure.entity.UserEntity;
import com.spring.backend.infrastructure.repository.CategoryJpaRepository;
import com.spring.backend.infrastructure.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUT {

  @Mock private CategoryJpaRepository categoryRepository;
  @Mock private UserJpaRepository userRepository;

  @InjectMocks private CategoryServiceImpl categoryService;

  @Test
  @DisplayName("createCategory should save and return dto")
  void createCategory_Works() {
    CategoryRequestDto dto = new CategoryRequestDto();
    dto.setName("Cat 1");

    categoryService.createCategory(dto);
    verify(categoryRepository).save(any());
  }

  @Test
  @DisplayName("getAllCategories should map creators correctly")
  void getAllCategories_Works() {
    CategoryEntity cat = new CategoryEntity();
    cat.setCreatedBy(1L);
    when(categoryRepository.findAll()).thenReturn(List.of(cat));
    when(userRepository.findById(1L)).thenReturn(Optional.of(new UserEntity()));

    List<CategoryResponseDto> result = categoryService.getAllCategories();
    assertThat(result).hasSize(1);
    verify(userRepository).findById(1L);
  }

  @Test
  @DisplayName("getCategoryById should return dto or fail")
  void getCategoryById_Works() {
    CategoryEntity cat = new CategoryEntity();
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

    categoryService.getCategoryById(1L);

    when(categoryRepository.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> categoryService.getCategoryById(2L))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("searchByName should return page")
  void searchByName_Works() {
    Page<CategoryEntity> page = new PageImpl<>(List.of(new CategoryEntity()));
    when(categoryRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(page);

    Page<CategoryResponseDto> result = categoryService.searchByName("test", 0, 10);
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("updateCategory should update and return")
  void updateCategory_Works() {
    CategoryEntity cat = new CategoryEntity();
    when(categoryRepository.findByIdAndIsActiveIsTrue(1L)).thenReturn(Optional.of(cat));

    CategoryRequestDto dto = new CategoryRequestDto();
    dto.setName("Updated");
    categoryService.updateCategory(1L, dto);

    assertThat(cat.getName()).isEqualTo("Updated");
    verify(categoryRepository).save(cat);
  }

  @Test
  @DisplayName("deleteCategory should deactivate category")
  void deleteCategory_Works() {
    CategoryEntity cat = new CategoryEntity();
    cat.setIsActive(true);
    when(categoryRepository.findByIdAndIsActiveIsTrue(1L)).thenReturn(Optional.of(cat));

    categoryService.deleteCategory(1L);

    assertThat(cat.getIsActive()).isFalse();
    verify(categoryRepository).save(cat);
  }
}
