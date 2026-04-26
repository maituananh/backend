package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.backend.controller.category.CategoryController;
import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CategoryControllerUT {

  private MockMvc mockMvc;

  @Mock private CategoryService categoryService;

  @InjectMocks private CategoryController categoryController;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(categoryController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  @Test
  @DisplayName("createCategory should return created category")
  void createCategory_Works() throws Exception {
    CategoryRequestDto request = new CategoryRequestDto();
    request.setName("New Category");

    CategoryResponseDto response = new CategoryResponseDto();
    response.setId(1L);
    response.setName("New Category");

    when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("New Category"));
  }

  @Test
  @DisplayName("getAllCategories should return list")
  void getAllCategories_Works() throws Exception {
    CategoryResponseDto response = new CategoryResponseDto();
    response.setId(1L);
    response.setName("Cat 1");

    when(categoryService.getAllCategories()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Cat 1"));
  }

  @Test
  @DisplayName("searchCategories should return page")
  void searchCategories_Works() throws Exception {
    CategoryResponseDto response = new CategoryResponseDto();
    response.setId(1L);
    response.setName("Cat 1");

    Page<CategoryResponseDto> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

    when(categoryService.searchByName(any(), anyInt(), anyInt())).thenReturn(page);

    mockMvc
        .perform(
            get("/api/categories/search")
                .param("name", "Cat")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Cat 1"));
  }

  @Test
  @DisplayName("getCategoryById should return category")
  void getCategoryById_Works() throws Exception {
    CategoryResponseDto response = new CategoryResponseDto();
    response.setId(1L);
    response.setName("Cat 1");

    when(categoryService.getCategoryById(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/categories/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @DisplayName("updateCategory should return updated category")
  void updateCategory_Works() throws Exception {
    CategoryRequestDto request = new CategoryRequestDto();
    request.setName("Updated");

    CategoryResponseDto response = new CategoryResponseDto();
    response.setId(1L);
    response.setName("Updated");

    when(categoryService.updateCategory(anyLong(), any(CategoryRequestDto.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  @Test
  @DisplayName("deleteCategory should return ok")
  void deleteCategory_Works() throws Exception {
    doNothing().when(categoryService).deleteCategory(1L);

    mockMvc.perform(delete("/api/categories/1")).andExpect(status().isOk());
  }
}
