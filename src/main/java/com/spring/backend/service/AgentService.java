package com.spring.backend.service;

import com.spring.backend.dto.chat.ChatHistoryItemDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.dto.classifier.ConversationMessage;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.service.chat.AIIntent;
import com.spring.backend.service.chat.AbstractChatService;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

  private final AgentContextService contextService;
  private final AIClient aiClient;
  private final List<AbstractChatService> chatServices;
  private final ChatHistoryService chatHistoryService;

  public ChatResponseDto<?> process(String sessionId, String message, String fileUrl) {
    long start = System.currentTimeMillis();

    // 1. Load context
    Optional<AgentContext> existing = contextService.get(sessionId);

    AgentContext ctx;
    boolean needsClassify =
        existing.isEmpty()
            || existing.get().isComplete()
            || AgentAIStep.COMPLETED == existing.get().getStep();

    AbstractChatService chatService;
    if (needsClassify) {
      String intent = aiClient.classify(message);
      AIIntent aiIntent = AIIntent.valueOf(intent);

      chatService = getSpecificChatService(aiIntent);
      ctx = contextService.createNew(sessionId, intent);
    } else {
      ctx = existing.get();
      AIIntent aiIntent = AIIntent.valueOf(ctx.getIntent());

      chatService = getSpecificChatService(aiIntent);
      contextService.refreshTtl(sessionId);
    }

    // Add user message to history
    addMessage(ctx, "user", message);

    // 2. Delegate to specific service
    ChatResponseDto<?> response = chatService.process(ctx, message, fileUrl);

    // 3. Post-process: save history and context
    addMessage(ctx, "assistant", response.getData().getReply());
    contextService.save(ctx);

    long elapsed = System.currentTimeMillis() - start;

    // 4. Final metadata update
    ChatResponseDto<?> finalResponse =
        response.toBuilder()
            .meta(
                ChatResponseDto.ChatMeta.builder()
                    .sessionId(sessionId)
                    .responseTime(elapsed)
                    .build())
            .build();

    // 5. Store history for UI persistence
    chatHistoryService.save(
        sessionId,
        ChatHistoryItemDto.builder()
            .userMessage(message)
            .fileUrl(fileUrl)
            .aiResponse(finalResponse)
            .timestamp(Instant.now().toString())
            .build());

    return finalResponse;
  }

  private AbstractChatService getSpecificChatService(AIIntent aiIntent) {
    return chatServices.stream()
        .filter(s -> s.intent() == aiIntent)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No service for intent: " + aiIntent));
  }

  private void addMessage(AgentContext ctx, String role, String content) {
    if (ctx.getMessages() == null) {
      ctx.setMessages(new ArrayList<>());
    }
    ctx.getMessages()
        .add(
            ConversationMessage.builder()
                .role(role)
                .content(content)
                .timestamp(Instant.now().toString())
                .build());
  }
}
