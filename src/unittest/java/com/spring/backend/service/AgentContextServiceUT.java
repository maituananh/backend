package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.enums.AgentAIStep;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AgentContextServiceUT {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private AgentContextService agentContextService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(agentContextService, "ttlMinutes", 30L);
    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("save should set value in redis")
  void save_Works() {
    AgentContext ctx = AgentContext.builder().sessionId("sess").build();
    agentContextService.save(ctx);
    verify(valueOperations).set(eq("agent:session:sess"), eq(ctx), any());
  }

  @Test
  @DisplayName("get should return context from redis")
  void get_Works() {
    AgentContext ctx = AgentContext.builder().sessionId("sess").build();
    when(valueOperations.get("agent:session:sess")).thenReturn(ctx);

    Optional<AgentContext> result = agentContextService.get("sess");
    assertThat(result).isPresent();
    assertThat(result.get().getSessionId()).isEqualTo("sess");
  }

  @Test
  @DisplayName("delete should remove key")
  void delete_Works() {
    agentContextService.delete("sess");
    verify(redisTemplate).delete("agent:session:sess");
  }

  @Test
  @DisplayName("createNew should save and return context")
  void createNew_Works() {
    AgentContext result = agentContextService.createNew("sess", "intent");
    assertThat(result.getIntent()).isEqualTo("intent");
    verify(valueOperations).set(eq("agent:session:sess"), any(), any());
  }

  @Test
  @DisplayName("markComplete should update status")
  void markComplete_Works() {
    AgentContext ctx = AgentContext.builder().sessionId("sess").build();
    when(valueOperations.get("agent:session:sess")).thenReturn(ctx);

    agentContextService.markComplete("sess");
    verify(valueOperations)
        .set(
            eq("agent:session:sess"),
            argThat(arg -> ((AgentContext) arg).getStep() == AgentAIStep.COMPLETED),
            any());
  }

  @Test
  @DisplayName("getTtlSeconds should return ttl")
  void getTtlSeconds_Works() {
    when(redisTemplate.getExpire("agent:session:sess", TimeUnit.SECONDS)).thenReturn(100L);
    assertThat(agentContextService.getTtlSeconds("sess")).isEqualTo(100L);
  }
}
