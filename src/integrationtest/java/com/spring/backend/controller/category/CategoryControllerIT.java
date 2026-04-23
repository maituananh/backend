package com.spring.backend.controller.category;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.category.CategoryRequestDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Category Controller Integration Tests")
class CategoryControllerIT extends BaseIntegrationTest {

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private UserRepository userRepository;

  private UserEntity testUser;

  @BeforeEach
  void setUp() {
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    SecurityContextHolder.clearContext();

    testUser =
        UserEntity.builder()
            .username("testuser")
            .password("password")
            .email("test@example.com")
            .cardId("CARD123")
            .phone("0123456789")
            .role(UserRole.ADMIN)
            .isActive(true)
            .build();
    userRepository.save(testUser);
  }

  private UserDetailsCustom getUserDetails() {
    return UserDetailsCustom.builder()
        .id(testUser.getId())
        .username(testUser.getUsername())
        .password(testUser.getPassword())
        .build();
  }

  @Test
  @DisplayName("POST /api/categories - returns 200 and creates category when authenticated")
  void createCategory_authenticated_returns200() throws Exception {
    CategoryRequestDto request = new CategoryRequestDto("Electronics", "Tech gadgets");
    mockMvc
        .perform(
            post("/api/categories")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Electronics"))
        .andExpect(jsonPath("$.note").value("Tech gadgets"))
        .andExpect(jsonPath("$.isActive").value(true));
  }

  @Test
  @DisplayName("POST /api/categories - returns 401 when unauthenticated")
  void createCategory_unauthenticated_returns401() throws Exception {
    CategoryRequestDto request = new CategoryRequestDto("Electronics", "Tech gadgets");

    mockMvc
        .perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("GET /api/categories - returns all categories (Public)")
  void getAllCategories_returns200() throws Exception {
    CategoryEntity cat1 = CategoryEntity.builder().name("Books").isActive(true).build();
    CategoryEntity cat2 = CategoryEntity.builder().name("Cloths").isActive(true).build();
    categoryRepository.saveAll(List.of(cat1, cat2));

    mockMvc
        .perform(get("/api/categories"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Books"))
        .andExpect(jsonPath("$[1].name").value("Cloths"));
  }

  @Test
  @DisplayName("GET /api/categories/search - returns paged categories (Public)")
  void searchCategories_returns200() throws Exception {
    for (int i = 1; i <= 5; i++) {
      categoryRepository.save(CategoryEntity.builder().name("Cat " + i).isActive(true).build());
    }

    mockMvc
        .perform(
            get("/api/categories/search")
                .param("name", "Cat")
                .param("page", "0")
                .param("size", "3"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.totalElements").value(5));
  }

  @Test
  @DisplayName("GET /api/categories/{id} - returns category by ID (Public)")
  void getCategoryById_returns200() throws Exception {
    CategoryEntity entity =
        categoryRepository.save(CategoryEntity.builder().name("Test Item").isActive(true).build());

    mockMvc
        .perform(get("/api/categories/" + entity.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(entity.getId()))
        .andExpect(jsonPath("$.name").value("Test Item"));
  }

  @Test
  @DisplayName("PUT /api/categories/{id} - updates category when authenticated")
  void updateCategory_authenticated_returns200() throws Exception {
    CategoryEntity entity =
        categoryRepository.save(CategoryEntity.builder().name("Old Name").isActive(true).build());
    CategoryRequestDto request = new CategoryRequestDto("New Name", "Updated note");

    mockMvc
        .perform(
            put("/api/categories/" + entity.getId())
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"))
        .andExpect(jsonPath("$.note").value("Updated note"));
  }

  @Test
  @DisplayName("DELETE /api/categories/{id} - soft deletes category when authenticated")
  void deleteCategory_authenticated_returns200() throws Exception {
    CategoryEntity entity =
        categoryRepository.save(CategoryEntity.builder().name("To Delete").isActive(true).build());

    mockMvc
        .perform(delete("/api/categories/" + entity.getId()).with(user(getUserDetails())))
        .andDo(print())
        .andExpect(status().isOk());

    CategoryEntity deleted = categoryRepository.findById(entity.getId()).orElseThrow();
    assert !deleted.getIsActive();
  }
}
