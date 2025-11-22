package com.spring.backend.controller.category;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.backend.BaseIntegrationTest;
import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class CategoryControllerTest extends BaseIntegrationTest {

  @Autowired private CategoryRepository categoryRepository;

  @BeforeEach
  void setUp() {
    categoryRepository.deleteAll();
    CategoryEntity category = new CategoryEntity();
    category.setName("Electronics");
    categoryRepository.save(category);
  }

  @Test
  @WithMockUser
  void createCategory_shouldReturnCategory() throws Exception {
    CategoryRequestDto request = new CategoryRequestDto();
    request.setName("Books");

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Books"));
  }

  @Test
  @WithMockUser
  void getAllCategories_shouldReturnList() throws Exception {
    mockMvc
        .perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").value("Electronics"));
  }
}
