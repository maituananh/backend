package com.spring.backend.controller.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.backend.BaseIntegrationTest;
import com.spring.backend.dto.auth.AuthRequestDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.TokenRepository;
import com.spring.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    tokenRepository.deleteAll();
    userRepository.deleteAll();
    UserEntity user = new UserEntity();
    user.setUsername("testuser");
    user.setPassword(passwordEncoder.encode("password"));
    user.setEmail("test@example.com");
    user.setCardId("123456789");
    user.setPhone("1234567890");
    user.setRole(UserRole.CUSTOMER);
    userRepository.save(user);
  }

  @Test
  void getToken_shouldReturnToken_whenCredentialsAreValid() throws Exception {
    AuthRequestDto request = new AuthRequestDto("testuser", "password");

    mockMvc
        .perform(
            post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  void getToken_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
    AuthRequestDto request = new AuthRequestDto("testuser", "wrongpassword");

    mockMvc
        .perform(
            post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logout_shouldReturnNoContent() throws Exception {
    // First login to get token
    AuthRequestDto loginRequest = new AuthRequestDto("testuser", "password");
    String response =
        mockMvc
            .perform(
                post("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String accessToken = objectMapper.readTree(response).get("accessToken").asText();

    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());
  }
}
