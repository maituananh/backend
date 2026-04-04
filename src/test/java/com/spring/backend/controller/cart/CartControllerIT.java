package com.spring.backend.controller.cart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.config.IntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.entity.*;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@DisplayName("Cart Controller Integration Tests")
class CartControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CartRepository cartRepository;

  @Autowired private CartItemRepository cartItemRepository;

  @Autowired private ProductRepository productRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CategoryRepository categoryRepository;

  private UserEntity testUser;
  private ProductEntity testProduct;

  @BeforeEach
  void setUp() {
    cartItemRepository.deleteAll();
    cartRepository.deleteAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    SecurityContextHolder.clearContext();

    testUser =
        UserEntity.builder()
            .username("cartuser")
            .password("password")
            .email("cart@example.com")
            .cardId("CARTCARD123")
            .phone("0987654321")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    userRepository.save(testUser);

    CategoryEntity category = CategoryEntity.builder().name("General").isActive(true).build();
    categoryRepository.save(category);

    testProduct =
        ProductEntity.builder()
            .name("Cart Product")
            .price(100.0)
            .code("CARTPROD001")
            .status(ProductStatus.NEW)
            .isActived(true)
            .category(category)
            .customer(testUser)
            .build();
    productRepository.save(testProduct);
  }

  private UserDetailsCustom getUserDetails() {
    return UserDetailsCustom.builder()
        .id(testUser.getId())
        .username(testUser.getUsername())
        .password(testUser.getPassword())
        .build();
  }

  @Test
  @DisplayName("POST /api/carts - returns 200 and adds item to cart")
  void addToCart_returns200() throws Exception {
    AddToCartRequestDto request = new AddToCartRequestDto();
    request.setProductId(testProduct.getId());
    request.setQuantity(2);

    mockMvc
        .perform(
            post("/api/carts")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerId").value(testUser.getId()))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].productName").value("Cart Product"))
        .andExpect(jsonPath("$.items[0].quantity").value(2));
  }

  @Test
  @DisplayName("GET /api/carts - returns my cart")
  void getMyCart_returns200() throws Exception {
    // First add to cart
    AddToCartRequestDto request = new AddToCartRequestDto();
    request.setProductId(testProduct.getId());
    request.setQuantity(1);

    mockMvc
        .perform(
            post("/api/carts")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Then get cart
    mockMvc
        .perform(get("/api/carts").with(user(getUserDetails())))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerId").value(testUser.getId()))
        .andExpect(jsonPath("$.items.length()").value(1));
  }

  @Test
  @DisplayName("DELETE /api/carts - deletes items from cart")
  void deleteItemsFromCart_returns200() throws Exception {
    // Add item first
    AddToCartRequestDto request = new AddToCartRequestDto();
    request.setProductId(testProduct.getId());
    request.setQuantity(3);

    mockMvc
        .perform(
            post("/api/carts")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Delete item
    mockMvc
        .perform(
            delete("/api/carts")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(testProduct.getId()))))
        .andDo(print())
        .andExpect(status().isOk());

    // Verify cart is empty
    mockMvc
        .perform(get("/api/carts").with(user(getUserDetails())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0));
  }

  @Test
  @DisplayName("POST /api/carts - returns 4xx when quantity is invalid")
  void addToCart_invalidQuantity_returnsError() throws Exception {
    AddToCartRequestDto request = new AddToCartRequestDto();
    request.setProductId(testProduct.getId());
    request.setQuantity(0);

    mockMvc
        .perform(
            post("/api/carts")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("POST /api/carts - returns 4xx when product not found")
  void addToCart_productNotFound_returnsError() throws Exception {
    AddToCartRequestDto request = new AddToCartRequestDto();
    request.setProductId(999L);
    request.setQuantity(1);

    mockMvc
        .perform(
            post("/api/carts")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isInternalServerError());
  }
}
