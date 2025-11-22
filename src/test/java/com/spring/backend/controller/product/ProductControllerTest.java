package com.spring.backend.controller.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.backend.BaseIntegrationTest;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

class ProductControllerTest extends BaseIntegrationTest {

  @Autowired private ProductRepository productRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private CategoryEntity category;
  private UserEntity user;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();

    category = new CategoryEntity();
    category.setName("Electronics");
    category = categoryRepository.save(category);

    user = new UserEntity();
    user.setUsername("testuser");
    user.setPassword(passwordEncoder.encode("password"));
    user.setEmail("test@example.com");
    user.setCardId("123456789");
    user.setPhone("1234567890");
    user.setRole(com.spring.backend.enums.UserRole.CUSTOMER);
    user = userRepository.save(user);

    ProductEntity product = new ProductEntity();
    product.setName("Laptop");
    product.setPrice(1000.0);
    product.setCategory(category);
    product.setCustomer(user);
    productRepository.save(product);
  }

  @Test
  @WithMockUser(username = "testuser")
  void createProduct_shouldReturnProduct() throws Exception {
    ProductRequestDto request = new ProductRequestDto();
    request.setName("Phone");
    request.setPrice(500.0);
    request.setCategoryId(category.getId());
    request.setCustomerId(user.getId());
    request.setDailyProfit(10.0);
    request.setQuantity(10);
    request.setType("Electronics");
    request.setCode("PHONE123");
    request.setStartedAt(java.time.Instant.now());
    request.setEndAt(java.time.Instant.now().plusSeconds(86400));
    request.setImageIds(java.util.Collections.emptyList());

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Phone"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void getAll_shouldReturnList() throws Exception {
    mockMvc
        .perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").value("Laptop"));
  }

  @Test
  @WithMockUser(username = "testuser")
  void searchProduct_shouldReturnPage() throws Exception {
    mockMvc
        .perform(
            get("/api/products/search")
                .param("name", "Laptop")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].name").value("Laptop"));
  }
}
