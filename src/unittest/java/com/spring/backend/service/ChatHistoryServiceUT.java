package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.chat.ChatHistoryItemDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceUT {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ListOperations<String, Object> listOperations;

  @InjectMocks private ChatHistoryService chatHistoryService;

  @Test
  @DisplayName("save should push to list and set expire")
  void save_Works() {
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    ChatHistoryItemDto item = new ChatHistoryItemDto();

    chatHistoryService.save("sess", item);

    verify(listOperations).rightPush(eq("chat:history:sess"), eq(item));
    verify(redisTemplate).expire(eq("chat:history:sess"), any());
  }

  @Test
  @DisplayName("getHistory should return list")
  void getHistory_Works() {
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    ChatHistoryItemDto item = new ChatHistoryItemDto();
    when(listOperations.range(anyString(), anyLong(), anyLong())).thenReturn(List.of(item));

    List<ChatHistoryItemDto> result = chatHistoryService.getHistory("sess");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("clear should delete key")
  void clear_Works() {
    chatHistoryService.clear("sess");
    verify(redisTemplate).delete("chat:history:sess");
  }
}
