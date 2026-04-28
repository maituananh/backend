package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.controller.auth.AuthController;
import com.spring.backend.dto.auth.AuthRequestDto;
import com.spring.backend.dto.auth.AuthResponseDto;
import com.spring.backend.dto.auth.RenewTokenRequestDto;
import com.spring.backend.service.AuthService;
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
class AuthControllerUT {

  private MockMvc mockMvc;

  @Mock private AuthService authService;

  @InjectMocks private AuthController authController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
  }

  @Test
  @DisplayName("getToken should return response")
  void getToken_Works() throws Exception {
    AuthRequestDto request = AuthRequestDto.builder().username("user").build();
    AuthResponseDto response = AuthResponseDto.builder().accessToken("access").build();

    when(authService.createToken(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access"));
  }

  @Test
  @DisplayName("logout should return no content")
  void logout_Works() throws Exception {
    doNothing().when(authService).handleLogout(anyString());

    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("renewToken should return new response")
  void renewToken_Works() throws Exception {
    RenewTokenRequestDto request = RenewTokenRequestDto.builder().refreshToken("refresh").build();
    AuthResponseDto response = AuthResponseDto.builder().accessToken("new_access").build();

    when(authService.handleRenewToken(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new_access"));
  }
}
