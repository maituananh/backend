package com.spring.backend.controller.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.backend.BaseIntegrationTest;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.TokenRepository;
import com.spring.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;

class UserControllerTest extends BaseIntegrationTest {

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
    user.setRole(com.spring.backend.enums.UserRole.CUSTOMER);
    userRepository.save(user);
  }

  @Test
  @WithUserDetails("testuser")
  void createUser_shouldReturnUser_whenDataIsValid() throws Exception {
    UserDto userDto = new UserDto();
    userDto.setUsername("newuser");
    userDto.setEmail("new@example.com");
    userDto.setCardId("987654321");
    userDto.setPhone("0987654321");

    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newuser"));
  }

  @Test
  @WithUserDetails("testuser")
  void getAllUsers_shouldReturnList() throws Exception {
    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithUserDetails("testuser")
  void getMyInfo_shouldReturnCurrentUser() throws Exception {
    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testuser"));
  }
}
