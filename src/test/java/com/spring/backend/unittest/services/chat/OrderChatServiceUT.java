package com.spring.backend.unittest.services.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.OrderChatResponseDto;
import com.spring.backend.dto.chat.OrderResultDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.service.AIClient;
import com.spring.backend.service.chat.AIIntent;
import com.spring.backend.service.chat.OrderChatService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderChatServiceUT {

  @Mock private AIClient aiClient;
  @Mock private OrderRepository orderRepository;
  @Mock private UserHelper userHelper;

  @InjectMocks private OrderChatService orderChatService;

  private AgentContext ctx;

  @BeforeEach
  void setUp() {
    ctx =
        AgentContext.builder()
            .intent(AIIntent.ORDER.name())
            .collectedParams(new HashMap<>())
            .messages(new ArrayList<>())
            .build();
  }

  @Test
  @DisplayName("process with ALL intent should return all user orders")
  void process_AllOrders() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(aiClient.chat(anyString(), anyString())).thenReturn("ALL");

    OrderEntity order =
        OrderEntity.builder()
            .id(100L)
            .status(OrderStatus.CONFIRMED)
            .totalAmount(BigDecimal.valueOf(50.0))
            .build();
    when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));

    // Act
    ChatResponseDto<?> response = orderChatService.process(ctx, "show me all my orders", null);

    // Assert
    assertThat(response).isInstanceOf(OrderChatResponseDto.class);
    assertThat(ctx.getStep()).isEqualTo(AgentAIStep.COMPLETED);

    OrderResultDto result = (OrderResultDto) response.getData().getResult();
    assertThat(result.getOrders()).hasSize(1);
    assertThat(result.getMessage()).contains("Found 1 orders");
  }

  @Test
  @DisplayName("process with specific status intent should return filtered orders")
  void process_SpecificStatus() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(aiClient.chat(anyString(), anyString())).thenReturn("CONFIRMED");

    OrderEntity order =
        OrderEntity.builder()
            .id(101L)
            .status(OrderStatus.CONFIRMED)
            .totalAmount(BigDecimal.valueOf(100.0))
            .build();
    when(orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, OrderStatus.CONFIRMED))
        .thenReturn(List.of(order));

    // Act
    ChatResponseDto<?> response = orderChatService.process(ctx, "show my confirmed orders", null);

    // Assert
    assertThat(response.getData().isComplete()).isTrue();
    OrderResultDto result = (OrderResultDto) response.getData().getResult();
    assertThat(result.getOrders()).hasSize(1);
    assertThat(result.getMessage()).isEqualTo("Found 1 orders confirmed");
  }

  @Test
  @DisplayName("process with UNKNOWN intent should ask user for clarity")
  void process_UnknownStatus() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(aiClient.chat(anyString(), anyString())).thenReturn("UNKNOWN");

    // Act
    ChatResponseDto<?> response = orderChatService.process(ctx, "gibberish", null);

    // Assert
    assertThat(response.getData().isComplete()).isFalse();
    assertThat(response.getData().getReply()).contains("What status would you like to see");
    assertThat(ctx.getStep()).isEqualTo(AgentAIStep.IN_PROGRESS);
  }

  @Test
  @DisplayName("process fallback to ALL if AI status is invalid")
  void process_InvalidStatusFallback() {
    // Arrange
    when(userHelper.getCurrentUserId()).thenReturn(1L);
    when(aiClient.chat(anyString(), anyString())).thenReturn("NOT_A_STATUS");
    when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

    // Act
    ChatResponseDto<?> response = orderChatService.process(ctx, "invalid", null);

    // Assert
    assertThat(response.getData().isComplete()).isTrue();
    assertThat(response.getData().getReply()).startsWith("Found 0 orders");
  }

  @Test
  @DisplayName("intent should return ORDER")
  void intent_ReturnsCorrectValue() {
    assertThat(orderChatService.intent()).isEqualTo(AIIntent.ORDER);
  }
}
