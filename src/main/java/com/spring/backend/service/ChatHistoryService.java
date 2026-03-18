package com.spring.backend.service;

import com.spring.backend.dto.chat.ChatHistoryItemDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryService {

  private final RedisTemplate<String, Object> redisTemplate;

  private static final String HISTORY_KEY_PREFIX = "chat:history:";
  private static final long HISTORY_TTL_HOURS = 30;

  public void save(String sessionId, ChatHistoryItemDto item) {
    String key = buildKey(sessionId);
    redisTemplate.opsForList().rightPush(key, item);
    redisTemplate.expire(key, Duration.ofMinutes(HISTORY_TTL_HOURS));
    log.debug("Saved history item for session: {}", sessionId);
  }

  public List<ChatHistoryItemDto> getHistory(String sessionId) {
    String key = buildKey(sessionId);
    List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
    if (rawList == null) {
      return new ArrayList<>();
    }
    return rawList.stream().filter(Objects::nonNull).map(obj -> (ChatHistoryItemDto) obj).toList();
  }

  public void clear(String sessionId) {
    redisTemplate.delete(buildKey(sessionId));
    log.debug("Cleared history for session: {}", sessionId);
  }

  private String buildKey(String sessionId) {
    return HISTORY_KEY_PREFIX + sessionId;
  }
}
