package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.adapter.s3.dto.UploadFileDto;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  private UserEntity userEntity;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    userEntity =
        UserEntity.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .name("Test User")
            .avatar("avatar.jpg")
            .build();

    userDto =
        UserDto.builder()
            .username("testuser")
            .email("test@example.com")
            .name("Test User")
            .phone("123456789")
            .cardId("CARD123")
            .build();
  }

  @Test
  @DisplayName("getAll should return list of UserDto")
  void getAll_shouldReturnListOfUserDto() {
    // Arrange
    when(userRepository.findAll()).thenReturn(List.of(userEntity));
    when(s3Adapter.getUrl("avatar.jpg")).thenReturn("http://s3.com/avatar.jpg");

    // Act
    List<UserDto> result = userService.getAll();

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    assertThat(result.get(0).getAvatar()).isEqualTo("http://s3.com/avatar.jpg");
    verify(userRepository).findAll();
  }

  @Test
  @DisplayName("createUser should save and return UserDto")
  void createUser_shouldSaveAndReturnUserDto() {
    // Arrange
    when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

    // Act
    UserDto result = userService.createUser(userDto);

    // Assert
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Nested
  @DisplayName("getByIdCard Tests")
  class GetByIdCardTests {
    @Test
    @DisplayName("should return UserDto when user exists")
    void shouldReturnUserDtoWhenUserExists() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

      // Act
      UserDto result = userService.getByIdCard(1L);

      // Assert
      assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.getByIdCard(1L))
          .isInstanceOf(java.util.NoSuchElementException.class);
    }
  }

  @Test
  @DisplayName("getMyInfo should return current user info")
  void getMyInfo_shouldReturnCurrentUserDto() {
    // Arrange
    UserDetailsCustom userDetails = UserDetailsCustom.builder().id(1L).username("testuser").build();
    Authentication auth = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);

    try (MockedStatic<SecurityContextHolder> mockedSecurity =
        mockStatic(SecurityContextHolder.class)) {
      mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(auth);
      when(auth.getPrincipal()).thenReturn(userDetails);
      when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

      // Act
      UserDto result = userService.getMyInfo();

      // Assert
      assertThat(result.getUsername()).isEqualTo("testuser");
    }
  }

  @Test
  @DisplayName("searchUser should return page of UserDto")
  void searchUser_shouldReturnPageOfUserDto() {
    // Arrange
    Page<UserEntity> page = new PageImpl<>(List.of(userEntity));
    // Note: Specification mocking might be tricky due to static search method in Repository
    // but here we just mock the repository call.
    when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

    // Act
    Page<UserDto> result =
        userService.searchUser("name", "email", "phone", "cardId", "username", 0, 10);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("delete should call repository deleteById")
  void delete_shouldCallRepository() {
    // Act
    userService.delete(1L);

    // Assert
    verify(userRepository).deleteById(1L);
  }

  @Nested
  @DisplayName("updateMyInfo Tests")
  class UpdateMyInfoTests {
    @Test
    @DisplayName("should update and return user info when user exists")
    void shouldUpdateInfoWhenUserExists() {
      // Arrange
      UserDetailsCustom userDetails =
          UserDetailsCustom.builder().id(1L).username("testuser").build();
      Authentication auth = mock(Authentication.class);
      SecurityContext securityContext = mock(SecurityContext.class);

      try (MockedStatic<SecurityContextHolder> mockedSecurity =
          mockStatic(SecurityContextHolder.class)) {
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        UserDto result = userService.updateMyInfo(userDto);

        // Assert
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(UserEntity.class));
      }
    }

    @Test
    @DisplayName("should update info except cardId when cardId is null in dto")
    void shouldUpdateInfoExceptCardIdWhenNull() {
      // Arrange
      UserDetailsCustom userDetails =
          UserDetailsCustom.builder().id(1L).username("testuser").build();
      Authentication auth = mock(Authentication.class);
      SecurityContext securityContext = mock(SecurityContext.class);

      UserDto updateDto = UserDto.builder().name("New Name").cardId(null).build();
      userEntity.setCardId("EXISTING_CARD_ID");

      try (MockedStatic<SecurityContextHolder> mockedSecurity =
          mockStatic(SecurityContextHolder.class)) {
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.updateMyInfo(updateDto);

        // Assert
        verify(userRepository).save(argThat(user -> "EXISTING_CARD_ID".equals(user.getCardId())));
      }
    }

    @Test
    @DisplayName("should throw exception when user not found in database")
    void shouldThrowExceptionWhenUserNotFound() {
      // Arrange
      UserDetailsCustom userDetails =
          UserDetailsCustom.builder().id(1L).username("testuser").build();
      Authentication auth = mock(Authentication.class);
      SecurityContext securityContext = mock(SecurityContext.class);

      try (MockedStatic<SecurityContextHolder> mockedSecurity =
          mockStatic(SecurityContextHolder.class)) {
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateMyInfo(userDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
      }
    }
  }

  @Nested
  @DisplayName("updateUser Tests")
  class UpdateUserTests {
    @Test
    @DisplayName("should update user by id")
    void shouldUpdateUserById() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
      when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

      // Act
      UserDto result = userService.updateUser(1L, userDto);

      // Assert
      assertThat(result.getUsername()).isEqualTo("testuser");
      verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("should throw exception when user to update is not found")
    void shouldThrowExceptionWhenNotFound() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.updateUser(1L, userDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User not found");
    }
  }

  @Nested
  @DisplayName("uploadAvatar Tests")
  class UploadAvatarTests {
    @Test
    @DisplayName("should upload avatar and update user")
    void shouldUploadAndSave() {
      // Arrange
      MultipartFile file = mock(MultipartFile.class);
      UserDetailsCustom userDetails =
          UserDetailsCustom.builder().id(1L).username("testuser").build();
      Authentication auth = mock(Authentication.class);
      SecurityContext securityContext = mock(SecurityContext.class);
      UploadFileDto uploadFileDto = UploadFileDto.builder().key("new_avatar.jpg").build();

      try (MockedStatic<SecurityContextHolder> mockedSecurity =
          mockStatic(SecurityContextHolder.class)) {
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(s3Adapter.uploadFile(file)).thenReturn(uploadFileDto);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        UserDto result = userService.uploadAvatar(file);

        // Assert
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(s3Adapter).uploadFile(file);
        verify(userRepository).save(any(UserEntity.class));
      }
    }

    @Test
    @DisplayName("should throw exception when user not found during upload")
    void shouldThrowExceptionWhenNotFound() {
      // Arrange
      MultipartFile file = mock(MultipartFile.class);
      UserDetailsCustom userDetails =
          UserDetailsCustom.builder().id(1L).username("testuser").build();
      Authentication auth = mock(Authentication.class);
      SecurityContext securityContext = mock(SecurityContext.class);

      try (MockedStatic<SecurityContextHolder> mockedSecurity =
          mockStatic(SecurityContextHolder.class)) {
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.uploadAvatar(file))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
      }
    }
  }
}
