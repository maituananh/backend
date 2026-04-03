package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.CategoryServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  @Mock private CategoryRepository categoryRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private CategoryServiceImpl categoryService;

  private CategoryEntity categoryEntity;
  private UserEntity userEntity;
  private CategoryRequestDto categoryRequestDto;

  @BeforeEach
  void setUp() {
    userEntity = UserEntity.builder().id(1L).name("Creator").username("creator").build();
    categoryEntity =
        CategoryEntity.builder()
            .id(1L)
            .name("Electronics")
            .note("Gadgets and more")
            .isActive(true)
            .createdBy(1L)
            .build();

    categoryRequestDto = new CategoryRequestDto();
    categoryRequestDto.setName("Electronics");
    categoryRequestDto.setNote("Gadgets and more");
  }

  @Test
  @DisplayName("createCategory should save and return CategoryResponseDto")
  void createCategory_shouldSaveAndReturnDto() {
    // Arrange
    when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);

    // Act
    CategoryResponseDto result = categoryService.createCategory(categoryRequestDto);

    // Assert
    assertThat(result.getName()).isEqualTo("Electronics");
    verify(categoryRepository).save(any(CategoryEntity.class));
  }

  @Nested
  @DisplayName("getAllCategories Tests")
  class GetAllCategoriesTests {
    @Test
    @DisplayName("should return list with creator info when creator exists")
    void shouldReturnListWithCreator() {
      // Arrange
      when(categoryRepository.findAll()).thenReturn(List.of(categoryEntity));
      when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

      // Act
      List<CategoryResponseDto> result = categoryService.getAllCategories();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getName()).isEqualTo("Electronics");
      assertThat(result.get(0).getCreatedByUser().getName()).isEqualTo("Creator");
    }

    @Test
    @DisplayName("should return list without creator info when creator is null in entity")
    void shouldReturnListWithoutCreatorWhenNullInEntity() {
      // Arrange
      categoryEntity.setCreatedBy(null);
      when(categoryRepository.findAll()).thenReturn(List.of(categoryEntity));

      // Act
      List<CategoryResponseDto> result = categoryService.getAllCategories();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getCreatedByUser()).isNull();
      verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("should return list without creator info when creator not found in database")
    void shouldReturnListWithoutCreatorWhenNotFoundInDb() {
      // Arrange
      when(categoryRepository.findAll()).thenReturn(List.of(categoryEntity));
      when(userRepository.findById(1L)).thenReturn(Optional.empty());

      // Act
      List<CategoryResponseDto> result = categoryService.getAllCategories();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getCreatedByUser()).isNull();
    }

    @Test
    @DisplayName("should return empty list when no categories exist")
    void shouldReturnEmptyList() {
      // Arrange
      when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

      // Act
      List<CategoryResponseDto> result = categoryService.getAllCategories();

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getCategoryById Tests")
  class GetCategoryByIdTests {
    @Test
    @DisplayName("should return category when found")
    void shouldReturnCategory() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
      when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

      // Act
      CategoryResponseDto result = categoryService.getCategoryById(1L);

      // Assert
      assertThat(result.getName()).isEqualTo("Electronics");
      assertThat(result.getCreatedByUser().getName()).isEqualTo("Creator");
    }

    @Test
    @DisplayName("should throw error when category not found")
    void shouldThrowErrorWhenCategoryNotFound() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> categoryService.getCategoryById(1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Category not found");
    }

    @Test
    @DisplayName("should return category even if creator info is missing")
    void shouldReturnCategoryEvenIfCreatorMissing() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
      when(userRepository.findById(1L)).thenReturn(Optional.empty());

      // Act
      CategoryResponseDto result = categoryService.getCategoryById(1L);

      // Assert
      assertThat(result.getName()).isEqualTo("Electronics");
      assertThat(result.getCreatedByUser()).isNull();
    }
  }

  @Test
  @DisplayName("searchByName should return page of categories")
  void searchByName_shouldReturnPage() {
    // Arrange
    Page<CategoryEntity> page = new PageImpl<>(List.of(categoryEntity));
    when(categoryRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(page);
    when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

    // Act
    Page<CategoryResponseDto> result = categoryService.searchByName("Electronics", 0, 10);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Electronics");
    assertThat(result.getContent().get(0).getCreatedByUser().getName()).isEqualTo("Creator");
  }

  @Nested
  @DisplayName("updateCategory Tests")
  class UpdateCategoryTests {
    @Test
    @DisplayName("should update when found")
    void shouldUpdate() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
      when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

      // Act
      CategoryResponseDto result = categoryService.updateCategory(1L, categoryRequestDto);

      // Assert
      assertThat(result.getName()).isEqualTo("Electronics");
      verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("should throw error when not found")
    void shouldThrowError() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> categoryService.updateCategory(1L, categoryRequestDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Category not found");
    }
  }

  @Nested
  @DisplayName("deleteCategory Tests")
  class DeleteCategoryTests {
    @Test
    @DisplayName("should soft delete when found")
    void shouldSoftDelete() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));

      // Act
      categoryService.deleteCategory(1L);

      // Assert
      assertThat(categoryEntity.getIsActive()).isFalse();
      verify(categoryRepository).save(categoryEntity);
    }

    @Test
    @DisplayName("should throw error when not found")
    void shouldThrowError() {
      // Arrange
      when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> categoryService.deleteCategory(1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Category not found");
    }
  }
}
