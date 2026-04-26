package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.service.chat.AIIntent;
import com.spring.backend.service.chat.AbstractChatService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentServiceUT {

  @Mock private AgentContextService contextService;
  @Mock private AIClient aiClient;
  @Mock private AbstractChatService chatService;
  @Mock private ChatHistoryService chatHistoryService;

  private AgentService agentService;

  @BeforeEach
  void setUp() {
    lenient().when(chatService.intent()).thenReturn(AIIntent.ORDER);
    agentService =
        new AgentService(contextService, aiClient, List.of(chatService), chatHistoryService);
  }

  @Test
  @DisplayName("process should classify and create new context if none exists")
  void process_NewContext() {
    String sessionId = "sess";
    String msg = "hello";

    when(contextService.get(sessionId)).thenReturn(Optional.empty());
    when(aiClient.classify(msg)).thenReturn("ORDER");

    AgentContext ctx = AgentContext.builder().intent("ORDER").messages(new ArrayList<>()).build();
    when(contextService.createNew(sessionId, "ORDER")).thenReturn(ctx);

    ChatResponseDto response =
        ChatResponseDto.builder()
            .data(ChatResponseDto.ChatData.builder().reply("hi").build())
            .build();
    doReturn(response).when(chatService).process(any(), any(), any());

    ChatResponseDto<?> result = agentService.process(sessionId, msg, null);

    assertThat(result.getData()).isNotNull();
    verify(aiClient).classify(msg);
    verify(contextService).createNew(sessionId, "ORDER");
  }

  @Test
  @DisplayName("process should use existing context if valid")
  void process_ExistingContext() {
    String sessionId = "sess";
    String msg = "more";

    AgentContext ctx =
        AgentContext.builder()
            .intent("ORDER")
            .step(AgentAIStep.IN_PROGRESS)
            .messages(new ArrayList<>())
            .build();
    when(contextService.get(sessionId)).thenReturn(Optional.of(ctx));

    ChatResponseDto response =
        ChatResponseDto.builder()
            .data(ChatResponseDto.ChatData.builder().reply("ok").build())
            .build();
    doReturn(response).when(chatService).process(any(), any(), any());

    agentService.process(sessionId, msg, null);

    verify(aiClient, never()).classify(any());
    verify(contextService).refreshTtl(sessionId);
  }
}
