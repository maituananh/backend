package com.spring.backend.controller.file;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.FileUploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

class FileUploadControllerIT extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;

  @MockitoBean private FileUploadService fileUploadService;

  private UserEntity testUser;

  @BeforeEach
  void setUp() {
    testUser =
        UserEntity.builder()
            .username("uploader")
            .email("uploader@example.com")
            .password("password")
            .name("Uploader User")
            .phone("123456789")
            .cardId("CARD_UPLOADER")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    testUser = userRepository.save(testUser);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  private UserDetailsCustom toUserDetails(UserEntity user) {
    return UserDetailsCustom.builder()
        .id(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .build();
  }

  @Test
  void shouldUploadFile() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test-image.jpg", "image/jpeg", "mock-image-content".getBytes());

    ImageResponseDto responseDto =
        ImageResponseDto.builder().id(1L).url("http://s3.example.com/test-image.jpg").build();

    when(fileUploadService.upload(any(MultipartFile.class))).thenReturn(responseDto);

    mockMvc
        .perform(multipart("/api/files/upload").file(file).with(user(toUserDetails(testUser))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.url").value("http://s3.example.com/test-image.jpg"));
  }
}
