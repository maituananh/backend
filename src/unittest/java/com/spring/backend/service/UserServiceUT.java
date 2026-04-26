package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.adapter.s3.dto.UploadFileDto;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceUT {

  @Mock private UserRepository userRepository;
  @Mock private S3Adapter s3Adapter;

  @InjectMocks private UserService userService;

  @Test
  @DisplayName("getAll should return list")
  void getAll_Works() {
    when(userRepository.findAll()).thenReturn(List.of(new UserEntity()));
    List<UserDto> result = userService.getAll();
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("createUser should save and return dto")
  void createUser_Works() {
    UserDto dto = UserDto.builder().username("user").build();
    UserEntity entity = new UserEntity();
    entity.setUsername("user");

    when(userRepository.save(any())).thenReturn(entity);

    UserDto result = userService.createUser(dto);
    assertThat(result.getUsername()).isEqualTo("user");
  }

  @Test
  @DisplayName("getByIdCard should return dto")
  void getByIdCard_Works() {
    UserEntity entity = new UserEntity();
    entity.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

    UserDto result = userService.getByIdCard(1L);
    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("getMyInfo should return current user info")
  void getMyInfo_Works() {
    try (MockedStatic<SecurityContextHolder> mockedContext =
        mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext(mockedContext, 1L);
      UserEntity entity = new UserEntity();
      entity.setId(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

      UserDto result = userService.getMyInfo();
      assertThat(result.getId()).isEqualTo(1L);
    }
  }

  @Test
  @DisplayName("searchUser should return page")
  void searchUser_Works() {
    Page<UserEntity> page = new PageImpl<>(List.of(new UserEntity()));
    when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

    Page<UserDto> result = userService.searchUser("name", null, null, null, null, 0, 10);
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("delete should call repository")
  void delete_Works() {
    userService.delete(1L);
    verify(userRepository).deleteById(1L);
  }

  @Test
  @DisplayName("updateMyInfo should update and return")
  void updateMyInfo_Works() {
    try (MockedStatic<SecurityContextHolder> mockedContext =
        mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext(mockedContext, 1L);
      UserEntity entity = new UserEntity();
      entity.setId(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
      when(userRepository.save(any())).thenReturn(entity);

      UserDto dto = UserDto.builder().name("New Name").build();
      UserDto result = userService.updateMyInfo(dto);

      assertThat(entity.getName()).isEqualTo("New Name");
      verify(userRepository).save(entity);
    }
  }

  @Test
  @DisplayName("updateUser should update and return")
  void updateUser_Works() {
    UserEntity entity = new UserEntity();
    entity.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(userRepository.save(any())).thenReturn(entity);

    UserDto dto = UserDto.builder().name("Admin Update").build();
    UserDto result = userService.updateUser(1L, dto);

    assertThat(entity.getName()).isEqualTo("Admin Update");
  }

  @Test
  @DisplayName("uploadAvatar should upload and update user")
  void uploadAvatar_Works() {
    try (MockedStatic<SecurityContextHolder> mockedContext =
        mockStatic(SecurityContextHolder.class)) {
      setupSecurityContext(mockedContext, 1L);
      UserEntity entity = new UserEntity();
      entity.setId(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

      MultipartFile file = mock(MultipartFile.class);
      UploadFileDto uploadResult = new UploadFileDto();
      uploadResult.setKey("avatars/user1.jpg");
      when(s3Adapter.uploadFile(file)).thenReturn(uploadResult);
      when(userRepository.save(any())).thenReturn(entity);

      UserDto result = userService.uploadAvatar(file);

      assertThat(entity.getAvatar()).isEqualTo("avatars/user1.jpg");
      verify(userRepository).save(entity);
    }
  }

  private void setupSecurityContext(
      MockedStatic<SecurityContextHolder> mockedContext, Long userId) {
    SecurityContext context = mock(SecurityContext.class);
    Authentication auth = mock(Authentication.class);
    UserDetailsCustom principal = mock(UserDetailsCustom.class);

    when(principal.getId()).thenReturn(userId);
    when(auth.getPrincipal()).thenReturn(principal);
    when(context.getAuthentication()).thenReturn(auth);
    mockedContext.when(SecurityContextHolder::getContext).thenReturn(context);
  }
}
