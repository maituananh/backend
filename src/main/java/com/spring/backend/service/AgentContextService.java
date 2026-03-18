package com.spring.backend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.enums.AgentAIStep;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentContextService {
  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${agent.session.ttl-minutes:30}")
  private long ttlMinutes;

  private static final String KEY_PREFIX = "agent:session:";

  // ── CRUD ──────────────────────────────────────────────

  public void save(AgentContext ctx) {
    String key = buildKey(ctx.getSessionId());
    ctx.setUpdatedAt(Instant.now().toString());
    redisTemplate.opsForValue().set(key, ctx, Duration.ofMinutes(ttlMinutes));
    log.debug(
        "Saved context: {} intent={} step={}", ctx.getSessionId(), ctx.getIntent(), ctx.getStep());
  }

  public Optional<AgentContext> get(String sessionId) {
    Object raw = redisTemplate.opsForValue().get(buildKey(sessionId));
    if (raw == null) return Optional.empty();
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      AgentContext ctx = mapper.convertValue(raw, AgentContext.class);
      return Optional.of(ctx);
    } catch (Exception e) {
      log.warn("Failed to deserialize context for session {}", sessionId);
      return Optional.empty();
    }
  }

  public void delete(String sessionId) {
    redisTemplate.delete(buildKey(sessionId));
    log.debug("Deleted context: {}", sessionId);
  }

  // ── TTL refresh ───────────────────────────────────────

  public void refreshTtl(String sessionId) {
    redisTemplate.expire(buildKey(sessionId), Duration.ofMinutes(ttlMinutes));
  }

  public long getTtlSeconds(String sessionId) {
    Long ttl = redisTemplate.getExpire(buildKey(sessionId), TimeUnit.SECONDS);
    return ttl != null ? ttl : -1;
  }

  // ── Helpers ───────────────────────────────────────────

  public boolean exists(String sessionId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(sessionId)));
  }

  public AgentContext createNew(String sessionId, String intent) {
    AgentContext ctx =
        AgentContext.builder()
            .sessionId(sessionId)
            .intent(intent)
            //                .step(step)
            //                .stepTotal(stepTotal)
            .collectedParams(new HashMap<>())
            .messages(new java.util.ArrayList<>())
            .missingFields("")
            .collectedJson("{}")
            .createdAt(Instant.now().toString())
            .updatedAt(Instant.now().toString())
            .build();
    save(ctx);
    return ctx;
  }

  public void markComplete(String sessionId) {
    get(sessionId)
        .ifPresent(
            ctx -> {
              ctx.setStep(AgentAIStep.COMPLETED);
              ctx.setMissingFields("");
              save(ctx);
            });
  }

  public void markCancelled(String sessionId) {
    get(sessionId)
        .ifPresent(
            ctx -> {
              ctx.setStep(AgentAIStep.CANCELLED);
              save(ctx);
            });
  }

  // ── Key builder ───────────────────────────────────────

  private String buildKey(String sessionId) {
    return KEY_PREFIX + sessionId;
  }
}
