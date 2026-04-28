package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.controller.cart.CartController;
import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.service.CartService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CartControllerUT {

  private MockMvc mockMvc;

  @Mock private CartService cartService;

  @InjectMocks private CartController cartController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
  }

  @Test
  @DisplayName("addToCart should return response")
  void addToCart_Works() throws Exception {
    AddToCartRequestDto request = AddToCartRequestDto.builder().productId(1L).quantity(1).build();
    when(cartService.addToCart(any())).thenReturn(CartResponseDto.builder().build());

    mockMvc
        .perform(
            post("/api/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getMyCart should return response")
  void getMyCart_Works() throws Exception {
    when(cartService.getMyCart()).thenReturn(CartResponseDto.builder().build());

    mockMvc.perform(get("/api/carts")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("deleteItemOnCart should return ok")
  void deleteItemOnCart_Works() throws Exception {
    mockMvc
        .perform(
            delete("/api/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L))))
        .andExpect(status().isOk());

    verify(cartService).deleteItemOnCart(anyList());
  }
}
