package com.spring.backend.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class UserControllerIT extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private CartRepository cartRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private ImageRepository imageRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private CardRepository cardRepository;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private CategoryRepository categoryRepository;

  private UserEntity adminUser;

  @BeforeEach
  void setUp() {
    // Clean up in FK-safe order: children before parents
    cartItemRepository.deleteAll();
    cartRepository.deleteAll();
    orderItemRepository.deleteAll();
    paymentRepository.deleteAll();
    orderRepository.deleteAll();
    imageRepository.deleteAll();
    productRepository.deleteAll();
    cardRepository.deleteAll();
    tokenRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();

    adminUser =
        UserEntity.builder()
            .username("admin")
            .email("admin@example.com")
            .password("password")
            .name("Admin")
            .phone("000000000")
            .cardId("ADMIN_CARD")
            .role(UserRole.ADMIN)
            .isActive(true)
            .build();
    adminUser = userRepository.save(adminUser);
  }

  @Test
  void shouldCreateUser() throws Exception {
    UserDto userDto =
        UserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .name("Test User")
            .phone("123456789")
            .cardId("CARD123")
            .role(UserRole.CUSTOMER)
            .build();

    mockMvc
        .perform(
            post("/api/users")
                .with(user(toUserDetails(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));

    assertThat(userRepository.count()).isEqualTo(2); // admin + new user
  }

  @Test
  void shouldGetAllUsers() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("user1")
            .email("user1@example.com")
            .password("password")
            .name("User One")
            .phone("111111111")
            .cardId("CARD1")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    userRepository.save(user);

    mockMvc
        .perform(get("/api/users").with(user(toUserDetails(adminUser))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2)) // admin + user1
        .andExpect(jsonPath("$[*].username").value(Matchers.hasItem("user1")));
  }

  @Test
  void shouldGetMyInfo() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("me")
            .email("me@example.com")
            .password("password")
            .name("Me")
            .phone("222222222")
            .cardId("CARD2")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    user = userRepository.save(user);

    mockMvc
        .perform(get("/api/users/me").with(user(toUserDetails(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("me"))
        .andExpect(jsonPath("$.email").value("me@example.com"));
  }

  @Test
  void shouldUpdateMyInfo() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("me")
            .email("me@example.com")
            .password("password")
            .name("Me")
            .phone("222222222")
            .cardId("CARD2")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    user = userRepository.save(user);

    UserDto updateDto =
        UserDto.builder()
            .name("Updated Me")
            .email("updated@example.com")
            .phone("333333333")
            .cardId("CARD2_NEW")
            .build();

    mockMvc
        .perform(
            put("/api/users/me")
                .with(user(toUserDetails(user)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Me"))
        .andExpect(jsonPath("$.email").value("updated@example.com"));
  }

  @Test
  void shouldGetUserById() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("user1")
            .email("user1@example.com")
            .password("password")
            .name("User One")
            .phone("111111111")
            .cardId("123456789")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    user = userRepository.save(user);

    mockMvc
        .perform(get("/api/users/" + user.getId()).with(user(toUserDetails(adminUser))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("user1"));
  }

  @Test
  void shouldSearchUsers() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("searchme")
            .email("search@example.com")
            .password("password")
            .name("Search Me")
            .phone("444444444")
            .cardId("CARD_SEARCH")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    userRepository.save(user);

    mockMvc
        .perform(
            get("/api/users/search")
                .with(user(toUserDetails(adminUser)))
                .param("username", "searchme")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].username").value("searchme"));
  }

  @Test
  void shouldDeleteUser() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("delete")
            .email("delete@example.com")
            .password("password")
            .name("Delete Me")
            .phone("555555555")
            .cardId("CARD_DELETE")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    user = userRepository.save(user);

    mockMvc
        .perform(delete("/api/users/" + user.getId()).with(user(toUserDetails(adminUser))))
        .andExpect(status().isOk());

    assertThat(userRepository.existsById(user.getId())).isFalse();
  }

  @Test
  void shouldUpdateUserById() throws Exception {
    UserEntity user =
        UserEntity.builder()
            .username("toupdate")
            .email("old@example.com")
            .password("password")
            .name("Old Name")
            .phone("666666666")
            .cardId("CARD_UPDATE")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    user = userRepository.save(user);

    UserDto updateDto =
        UserDto.builder()
            .username("toupdate")
            .name("New Name")
            .email("new@example.com")
            .phone("666666666")
            .cardId("CARD_UPDATE")
            .role(UserRole.CUSTOMER)
            .build();

    mockMvc
        .perform(
            put("/api/users/" + user.getId())
                .with(user(toUserDetails(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"))
        .andExpect(jsonPath("$.email").value("new@example.com"));
  }

  private UserDetailsCustom toUserDetails(UserEntity user) {
    return UserDetailsCustom.builder()
        .id(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .build();
  }
}
