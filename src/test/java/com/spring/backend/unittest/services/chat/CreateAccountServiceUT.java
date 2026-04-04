package com.spring.backend.unittest.services.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.CreateAccountChatResponseDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.AIClient;
import com.spring.backend.service.chat.AIIntent;
import com.spring.backend.service.chat.CreateAccountService;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceUT {

  @Mock private AIClient aiClient;
  @Mock private UserRepository userRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;

  @InjectMocks private CreateAccountService createAccountService;

  private AgentContext ctx;

  @BeforeEach
  void setUp() {
    ctx =
        AgentContext.builder()
            .intent(AIIntent.CREATE_ACCOUNT.name())
            .collectedParams(new HashMap<>())
            .build();
  }

  @Test
  @DisplayName("process with text only should update context and return partial result")
  void process_TextOnly_Partial() {
    // Arrange
    String aiReplyJSON =
        "{\"status\": \"PARTIAL\", \"extracted\": {\"username\": \"testuser\"}, \"missing_fields\": [\"email\"], \"message\": \"Please provide email.\"}";
    when(aiClient.chat(anyString(), anyString())).thenReturn(aiReplyJSON);

    // Act
    ChatResponseDto<?> response =
        createAccountService.process(ctx, "my username is testuser", null);

    // Assert
    assertThat(response).isInstanceOf(CreateAccountChatResponseDto.class);
    assertThat(ctx.getCollectedParams().get("username")).isEqualTo("testuser");
    assertThat(ctx.getStep()).isEqualTo(AgentAIStep.IN_PROGRESS);
    assertThat(response.getData().getReply()).isEqualTo("Please provide email.");
  }

  @Test
  @DisplayName("process with image should update all fields and complete account creation")
  void process_WithImage_Complete_Success() {
    // Arrange
    String aiReplyJSON =
        "{\"status\": \"COMPLETE\", \"extracted\": {\"username\": \"testuser\", \"email\": \"t@e.com\", \"id_number\": \"123456789012\", \"full_name\": \"John Doe\", \"date_of_birth\": \"01/01/1990\"}, \"missing_fields\": [], \"message\": \"Success\"}";
    when(aiClient.chat(anyString(), anyString(), anyString())).thenReturn(aiReplyJSON);

    when(userRepository.existsByUsername("testuser")).thenReturn(false);
    when(userRepository.existsByEmail("t@e.com")).thenReturn(false);
    when(userRepository.existsByCardId("123456789012")).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

    // Act
    ChatResponseDto<?> response =
        createAccountService.process(ctx, "here is my card", "http://image.url");

    // Assert
    assertThat(ctx.getStep()).isEqualTo(AgentAIStep.COMPLETED);
    verify(userRepository).save(any(UserEntity.class));
    assertThat(response.getData().getReply()).isEqualTo("Success");
  }

  @Test
  @DisplayName("handleAccountCreation should flag duplicate user info")
  void handleAccountCreation_DuplicateInfo() {
    // Arrange
    String aiReplyJSON =
        "{\"status\": \"COMPLETE\", \"extracted\": {\"username\": \"dup\", \"email\": \"dup@e.com\", \"id_number\": \"000000000000\"}, \"missing_fields\": [], \"message\": \"Done\"}";
    when(aiClient.chat(anyString(), anyString())).thenReturn(aiReplyJSON);

    when(userRepository.existsByUsername("dup")).thenReturn(true);
    when(userRepository.existsByEmail("dup@e.com")).thenReturn(true);
    when(userRepository.existsByCardId("000000000000")).thenReturn(true);

    // Act
    ChatResponseDto<?> response = createAccountService.process(ctx, "info", null);

    // Assert
    assertThat(response.getData().getReply()).contains("already exists");
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("intent should return CREATE_ACCOUNT")
  void intent_ReturnsCorrectValue() {
    assertThat(createAccountService.intent()).isEqualTo(AIIntent.CREATE_ACCOUNT);
  }
}
