package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.backend.controller.product.ProductController;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.service.ProductService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProductControllerUT {

  private MockMvc mockMvc;

  @Mock private ProductService productService;

  @InjectMocks private ProductController productController;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(productController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  @Test
  @DisplayName("createProduct should return response")
  void createProduct_Works() throws Exception {
    ProductRequestDto request =
        ProductRequestDto.builder()
            .name("Test Product")
            .price(100.0)
            .dailyProfit(10.0)
            .stockQty(5)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(1))
            .categoryId(1L)
            .customerId(1L)
            .code("CODE")
            .imageIds(List.of(1L, 2L, 3L, 4L))
            .build();

    when(productService.createProduct(any()))
        .thenReturn(ProductResponseDto.builder().id(1L).build());

    mockMvc
        .perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getById should return detail")
  void getById_Works() throws Exception {
    when(productService.getById(1L)).thenReturn(ProductDetailResponseDto.builder().id(1L).build());

    mockMvc.perform(get("/api/products/1")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("search should return pagination")
  void search_Works() throws Exception {
    Pagination<ProductResponseDto> response =
        Pagination.<ProductResponseDto>builder().totalPages(1).build();
    when(productService.search(any())).thenReturn(response);

    mockMvc
        .perform(get("/api/products/search").param("page", "0").param("size", "10"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("deleteById should return ok")
  void deleteById_Works() throws Exception {
    mockMvc.perform(delete("/api/products/1")).andExpect(status().isOk());
    verify(productService).deleteById(1L);
  }
}
