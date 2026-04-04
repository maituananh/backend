package com.spring.backend.controller.product;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.config.IntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.ImageRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@DisplayName("Product Controller Integration Tests")
class ProductControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProductRepository productRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ImageRepository imageRepository;

  private UserEntity testUser;
  private CategoryEntity testCategory;
  private List<ImageEntity> testImages;

  @BeforeEach
  void setUp() {
    imageRepository.deleteAll();
    productRepository.deleteAll();
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

    testCategory = CategoryEntity.builder().name("Electronics").isActive(true).build();
    categoryRepository.save(testCategory);

    testImages = new ArrayList<>();
    for (int i = 1; i <= 4; i++) {
      ImageEntity img = ImageEntity.builder().fileName("image" + i + ".png").build();
      testImages.add(imageRepository.save(img));
    }
  }

  private UserDetailsCustom getUserDetails() {
    return UserDetailsCustom.builder()
        .id(testUser.getId())
        .username(testUser.getUsername())
        .password(testUser.getPassword())
        .build();
  }

  @Test
  @DisplayName("POST /api/products - returns 200 and creates product when authenticated")
  void createProduct_authenticated_returns200() throws Exception {
    ProductRequestDto request = new ProductRequestDto();
    request.setName("Smartphone");
    request.setPrice(1000.0);
    request.setDailyProfit(10.0);
    request.setStockQty(50);
    request.setStartDate(LocalDate.now());
    request.setEndDate(LocalDate.now().plusMonths(1));
    request.setCategoryId(testCategory.getId());
    request.setCustomerId(testUser.getId());
    request.setCode("PROD001");
    request.setImageIds(testImages.stream().map(ImageEntity::getId).toList());
    request.setDescription("A high-end smartphone");

    mockMvc
        .perform(
            post("/api/products")
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Smartphone"))
        .andExpect(jsonPath("$.price").value(1000.0))
        .andExpect(jsonPath("$.code").value("PROD001"));
  }

  @Test
  @DisplayName("GET /api/products - returns all products (Public)")
  void getAllProducts_returns200() throws Exception {
    ProductEntity product =
        ProductEntity.builder()
            .name("Laptop")
            .price(1500.0)
            .code("LAP001")
            .status(ProductStatus.NEW)
            .isActived(true)
            .category(testCategory)
            .customer(testUser)
            .build();
    productRepository.save(product);

    mockMvc
        .perform(get("/api/products"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Laptop"));
  }

  @Test
  @DisplayName("GET /api/products/{id} - returns product detail (Public)")
  void getProductById_returns200() throws Exception {
    ProductEntity product =
        ProductEntity.builder()
            .name("Tablet")
            .price(500.0)
            .code("TAB001")
            .status(ProductStatus.NEW)
            .isActived(true)
            .category(testCategory)
            .customer(testUser)
            .build();
    productRepository.save(product);

    mockMvc
        .perform(get("/api/products/" + product.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Tablet"))
        .andExpect(jsonPath("$.code").value("TAB001"));
  }

  @Test
  @DisplayName("GET /api/products/{id}/related - returns related products (Public)")
  void getRelatedProducts_returns200() throws Exception {
    ProductEntity product1 =
        ProductEntity.builder()
            .name("P1")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .stockQty(10)
            .availableQty(10)
            .isActived(true)
            .build();
    ProductEntity product2 =
        ProductEntity.builder()
            .name("P2")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .stockQty(10)
            .availableQty(10)
            .isActived(true)
            .build();
    productRepository.saveAll(List.of(product1, product2));

    mockMvc
        .perform(get("/api/products/" + product1.getId() + "/related"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/products/search - returns paged products (Public)")
  void searchProducts_returns200() throws Exception {
    ProductEntity product =
        ProductEntity.builder()
            .name("SearchMe")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .isActived(true)
            .build();
    productRepository.save(product);

    mockMvc
        .perform(get("/api/products/search").param("name", "SearchMe"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("SearchMe"));
  }

  @Test
  @DisplayName("DELETE /api/products/{id} - deletes product when authenticated")
  void deleteProduct_authenticated_returns200() throws Exception {
    ProductEntity product =
        ProductEntity.builder()
            .name("To Delete")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .isActived(true)
            .build();
    productRepository.save(product);

    mockMvc
        .perform(delete("/api/products/" + product.getId()).with(user(getUserDetails())))
        .andDo(print())
        .andExpect(status().isOk());

    assert !productRepository.existsById(product.getId())
        || !productRepository.findById(product.getId()).get().getIsActived();
  }

  @Test
  @DisplayName("PUT /api/products/{id} - updates product when authenticated")
  void updateProduct_authenticated_returns200() throws Exception {
    ProductEntity product =
        ProductEntity.builder()
            .name("Old Name")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .isActived(true)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .stockQty(50)
            .availableQty(50)
            .build();
    productRepository.save(product);

    ProductRequestDto request = new ProductRequestDto();
    request.setName("New Name");
    request.setPrice(1200.0);
    request.setDailyProfit(15.0);
    request.setStockQty(60);
    request.setStartDate(LocalDate.now());
    request.setEndDate(LocalDate.now().plusMonths(2));
    request.setCategoryId(testCategory.getId());
    request.setCustomerId(testUser.getId());
    request.setCode("PROD002");
    request.setImageIds(testImages.stream().map(ImageEntity::getId).toList());

    mockMvc
        .perform(
            put("/api/products/" + product.getId())
                .with(user(getUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"));
  }

  @Test
  @DisplayName("PATCH /api/products/{id}/liquidation - liquidates product when authenticated")
  void liquidationProduct_authenticated_returns200() throws Exception {
    ProductEntity product =
        ProductEntity.builder()
            .name("Liquidate Me")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .isActived(true)
            .build();
    productRepository.save(product);

    mockMvc
        .perform(
            patch("/api/products/" + product.getId() + "/liquidation").with(user(getUserDetails())))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/products/{id}/related - returns related products with images")
  void getRelatedProducts_withImages() throws Exception {
    ProductEntity product1 =
        ProductEntity.builder()
            .name("P1")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .stockQty(10)
            .availableQty(10)
            .isActived(true)
            .build();
    product1 = productRepository.save(product1);

    ProductEntity product2 =
        ProductEntity.builder()
            .name("P2")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.LIQUIDATION)
            .stockQty(10)
            .availableQty(10)
            .isActived(true)
            .build();
    product2 = productRepository.save(product2);

    ImageEntity existingImg = testImages.get(0);
    product2.setImages(new java.util.ArrayList<>(List.of(existingImg)));
    product2 = productRepository.save(product2);

    mockMvc
        .perform(get("/api/products/" + product1.getId() + "/related"))
        .andExpect(status().isOk());
  }

  @Autowired private com.spring.backend.service.ProductService productService;

  @Test
  @DisplayName("Exceptions mapping in ProductService direct checks")
  void testExceptionsInProductServiceDirect() {
    ProductRequestDto req = new ProductRequestDto();
    req.setName("Test");
    req.setPrice(100.0);
    req.setDailyProfit(10.0);
    req.setStockQty(50);
    req.setCode("TEST01");
    req.setCategoryId(testCategory.getId());
    req.setCustomerId(testUser.getId());
    req.setImageIds(testImages.stream().map(ImageEntity::getId).toList());

    // 1. StartDate/EndDate null
    req.setStartDate(null);
    req.setEndDate(null);
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> productService.createProduct(req));

    // 2. StartDate < today
    req.setStartDate(LocalDate.now().minusDays(1));
    req.setEndDate(LocalDate.now().plusDays(1));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> productService.createProduct(req));

    // 3. EndDate < StartDate
    req.setStartDate(LocalDate.now());
    req.setEndDate(LocalDate.now().minusDays(1));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> productService.createProduct(req));

    // Prepare Sold Out Product
    ProductEntity soldOutProduct =
        ProductEntity.builder()
            .name("SOLD OUT PROD")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.SOLD_OUT)
            .isActived(true)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .build();
    soldOutProduct = productRepository.save(soldOutProduct);

    // 4. updateById - SOLD OUT
    req.setStartDate(LocalDate.now());
    req.setEndDate(LocalDate.now().plusDays(5));
    final Long soldOutId = soldOutProduct.getId();
    org.junit.jupiter.api.Assertions.assertThrows(
        RuntimeException.class, () -> productService.updateById(soldOutId, req));

    // 5. liquidationProduct - SOLD OUT
    org.junit.jupiter.api.Assertions.assertThrows(
        RuntimeException.class, () -> productService.liquidationProduct(soldOutId));

    // Prepare Valid Product
    ProductEntity validProduct =
        ProductEntity.builder()
            .name("VALID")
            .category(testCategory)
            .customer(testUser)
            .status(ProductStatus.NEW)
            .isActived(true)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .build();
    validProduct = productRepository.save(validProduct);
    final Long validId = validProduct.getId();

    // 6. updateById - invalid image count
    req.setImageIds(List.of(testImages.get(0).getId()));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> productService.updateById(validId, req));

    // 7. updateById - newStartDate < oldStartDate
    req.setImageIds(testImages.stream().map(ImageEntity::getId).toList());
    req.setStartDate(LocalDate.now().minusDays(1));
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> productService.updateById(validId, req));
  }
}
