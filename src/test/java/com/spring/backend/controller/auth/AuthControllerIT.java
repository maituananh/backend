package com.spring.backend.controller.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.dto.auth.AuthRequestDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integration test for AuthController.
 *
 * <p>Use @IntegrationTest to automatically: - Start full Spring context with H2 in-memory DB -
 * Start WireMock server to mock external APIs - Start Embedded Redis - Activate "test" profile with
 * application-test.yml
 */
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIT extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    UserEntity user =
        UserEntity.builder()
            .username("admin")
            .password(passwordEncoder.encode("password123"))
            .email("admin@test.com")
            .cardId("123456789")
            .phone("0123456789")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    userRepository.save(user);
  }

  @Test
  @DisplayName("POST /api/auth/token - returns 200 when credentials are valid")
  void getToken_validCredentials_returns200() throws Exception {
    // GIVEN
    AuthRequestDto request = new AuthRequestDto("admin", "password123");

    // WHEN & THEN
    mockMvc
        .perform(
            post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  @DisplayName("POST /api/auth/token - returns 401/400 when credentials are wrong")
  void getToken_invalidCredentials_returnsError() throws Exception {
    // GIVEN
    AuthRequestDto request = new AuthRequestDto("notfound", "wrongpass");

    // WHEN & THEN
    mockMvc
        .perform(
            post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }
}
