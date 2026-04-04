package com.spring.backend.unittest.services.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.ProfileChatResponseDto;
import com.spring.backend.dto.chat.ProfileResultDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.AIClient;
import com.spring.backend.service.chat.AIIntent;
import com.spring.backend.service.chat.ProfileService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceUT {

  @Mock private UserRepository userRepository;
  @Mock private UserHelper userHelper;
  @Mock private S3Adapter s3Adapter;
  @Mock private AIClient aiClient;

  @InjectMocks private ProfileService profileService;

  private AgentContext ctx;
  private UserEntity user;

  @BeforeEach
  void setUp() {
    ctx =
        AgentContext.builder()
            .intent(AIIntent.PROFILE.name())
            .collectedParams(new HashMap<>())
            .messages(new ArrayList<>())
            .build();
    user =
        UserEntity.builder()
            .id(1L)
            .username("testuser")
            .name("John Doe")
            .age(30)
            .email("j@e.com")
            .avatar("avatar.jpg")
            .build();
  }

  @Test
  @DisplayName("process with profile update should update user and return profile")
  void process_ProfileUpdate() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    String aiReplyJSON =
        "{\"status\": \"COMPLETE\", \"extracted\": {\"age\": \"31\", \"full_name\": \"John Smith\"}, \"message\": \"Profile updated.\"}";
    lenient().when(aiClient.chat(anyString(), anyString())).thenReturn(aiReplyJSON);

    when(s3Adapter.getUrl(any())).thenReturn("http://s3.url/avatar.jpg");

    // Act
    ChatResponseDto<?> response =
        profileService.process(ctx, "I am 31 now and change my name to John Smith", null);

    // Assert
    assertThat(response).isInstanceOf(ProfileChatResponseDto.class);
    assertThat(ctx.getStep()).isEqualTo(AgentAIStep.COMPLETED);

    assertThat(user.getAge()).isEqualTo(31);
    assertThat(user.getName()).isEqualTo("John Smith");
    verify(userRepository).save(user);

    ProfileResultDto result = (ProfileResultDto) response.getData().getResult();
    assertThat(result.getName()).isEqualTo("John Smith");
    assertThat(result.getAge()).isEqualTo(31);
    assertThat(response.getData().getReply()).isEqualTo("Profile updated.");
  }

  @Test
  @DisplayName("process should handle non-integer age gracefully")
  void process_HandlesInvalidAge() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    String aiReplyJSON =
        "{\"status\": \"COMPLETE\", \"extracted\": {\"age\": \"invalid_age\"}, \"message\": \"OK\"}";
    lenient().when(aiClient.chat(anyString(), anyString())).thenReturn(aiReplyJSON);

    // Act
    profileService.process(ctx, "age is whatever", null);

    // Assert
    assertThat(user.getAge()).isEqualTo(30); // Unchanged due to catch in update logic
  }

  @Test
  @DisplayName("process should return error if user not found")
  void process_UserNotFound() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // Act
    ChatResponseDto<?> response = profileService.process(ctx, "profile", null);

    // Assert
    assertThat(response.getStatus()).isEqualTo("error");
  }

  @Test
  @DisplayName("intent should return PROFILE")
  void intent_ReturnsCorrectValue() {
    assertThat(profileService.intent()).isEqualTo(AIIntent.PROFILE);
  }
}
