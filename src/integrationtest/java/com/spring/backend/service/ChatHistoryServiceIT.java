package com.spring.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.dto.chat.ChatHistoryItemDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class ChatHistoryServiceIT extends BaseIntegrationTest {

  @Autowired private ChatHistoryService chatHistoryService;

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  private final String sessionId = "test-session-123";

  @BeforeEach
  void setUp() {
    chatHistoryService.clear(sessionId);
  }

  @AfterEach
  void tearDown() {
    chatHistoryService.clear(sessionId);
  }

  @Test
  void shouldSaveAndGetHistory() {
    // Given
    ChatHistoryItemDto item1 =
        ChatHistoryItemDto.builder().userMessage("message 1").timestamp("time-1").build();

    ChatResponseDto<Object> aiResponse = ChatResponseDto.builder().status("ok").build();

    ChatHistoryItemDto item2 =
        ChatHistoryItemDto.builder()
            .userMessage("message 2")
            .aiResponse(aiResponse)
            .timestamp("time-2")
            .build();

    // When
    chatHistoryService.save(sessionId, item1);
    chatHistoryService.save(sessionId, item2);

    // Then
    List<ChatHistoryItemDto> history = chatHistoryService.getHistory(sessionId);

    assertThat(history).hasSize(2);
    assertThat(history.get(0).getUserMessage()).isEqualTo("message 1");
    assertThat(history.get(1).getUserMessage()).isEqualTo("message 2");
    assertThat(history.get(1).getAiResponse().getStatus()).isEqualTo("ok");

    // Check if TTL is applied
    Long expire = redisTemplate.getExpire("chat:history:" + sessionId);
    assertThat(expire).isNotNull().isGreaterThan(0L);
  }

  @Test
  void shouldGetEmptyHistoryWhenNoData() {
    // When
    List<ChatHistoryItemDto> history = chatHistoryService.getHistory("unknown-session");

    // Then
    assertThat(history).isEmpty();
  }

  @Test
  void shouldClearHistory() {
    // Given
    ChatHistoryItemDto item =
        ChatHistoryItemDto.builder().userMessage("message").timestamp("time").build();
    chatHistoryService.save(sessionId, item);

    assertThat(chatHistoryService.getHistory(sessionId)).isNotEmpty();

    // When
    chatHistoryService.clear(sessionId);

    // Then
    assertThat(chatHistoryService.getHistory(sessionId)).isEmpty();
  }
}
