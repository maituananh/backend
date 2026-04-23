package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.controller.user.UserController;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.service.ProductService;
import com.spring.backend.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerUT {

  private MockMvc mockMvc;

  @Mock private UserService userService;
  @Mock private ProductService productService;

  @InjectMocks private UserController userController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(userController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  @Test
  @DisplayName("createUser should return user")
  void createUser_Works() throws Exception {
    UserDto user = UserDto.builder().username("user").build();
    when(userService.createUser(any())).thenReturn(user);

    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("user"));
  }

  @Test
  @DisplayName("getAllUsers should return list")
  void getAllUsers_Works() throws Exception {
    when(userService.getAll()).thenReturn(List.of(UserDto.builder().id(1L).build()));

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));
  }

  @Test
  @DisplayName("getMyInfo should return user")
  void getMyInfo_Works() throws Exception {
    when(userService.getMyInfo()).thenReturn(UserDto.builder().id(1L).build());

    mockMvc.perform(get("/api/users/me")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("searchUsers should return page")
  void searchUsers_Works() throws Exception {
    PageImpl<UserDto> page =
        new PageImpl<>(
            List.of(UserDto.builder().username("user").build()), PageRequest.of(0, 10), 1);
    when(userService.searchUser(any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(page);

    mockMvc
        .perform(get("/api/users/search").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].username").value("user"));
  }

  @Test
  @DisplayName("deleteUserById should return ok")
  void deleteUserById_Works() throws Exception {
    mockMvc.perform(delete("/api/users/1")).andExpect(status().isOk());
    verify(userService).delete(1L);
  }

  @Test
  @DisplayName("getProductsByUserId should return list")
  void getProductsByUserId_Works() throws Exception {
    when(productService.getProductsByUserId(1L)).thenReturn(List.of());

    mockMvc.perform(get("/api/users/1/products")).andExpect(status().isOk());
  }
}
