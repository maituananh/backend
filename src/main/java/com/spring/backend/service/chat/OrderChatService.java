package com.spring.backend.service.chat;

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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderChatService extends AbstractChatService {

  private final AIClient aiClient;
  private final OrderRepository orderRepository;
  private final UserHelper userHelper;

  @Override
  public ChatResponseDto<?> process(AgentContext ctx, String message, String fileUrl) {
    Long userId = userHelper.getCurrentUserId();

    // 1. Identify which order status they want to view using AI
    String statusStr = determineStatus(message);
    log.info("Determined status for user {}: {}", userId, statusStr);

    if (statusStr.equalsIgnoreCase("UNKNOWN")) {
      ctx.setStep(AgentAIStep.IN_PROGRESS);
      return ChatResponseDto.<OrderResultDto>builder()
          .status("success")
          .data(
              ChatResponseDto.ChatData.<OrderResultDto>builder()
                  .reply(
                      "What status would you like to see for your order? (For example: Awaiting Confirmation, In Transit, Completed, or All)")
                  .intent(intent().name())
                  .isComplete(false)
                  .messages(ctx.getMessages())
                  .build())
          .build();
    }

    // 2. Fetch orders
    List<OrderEntity> orders;
    if (statusStr.equalsIgnoreCase("ALL")) {
      orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    } else {
      try {
        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
        orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status determined: {}. Falling back to ALL.", statusStr);
        orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        statusStr = "ALL";
      }
    }

    ctx.setStep(AgentAIStep.COMPLETED);

    OrderResultDto result =
        OrderResultDto.builder()
            .message(
                "Found "
                    + orders.size()
                    + " orders "
                    + (statusStr.equalsIgnoreCase("ALL") ? "" : statusStr.toLowerCase()))
            .orders(orders.stream().map(this::toBriefDto).toList())
            .build();

    return OrderChatResponseDto.builder()
        .status("success")
        .data(
            ChatResponseDto.ChatData.<OrderResultDto>builder()
                .reply(result.getMessage())
                .intent(intent().name())
                .isComplete(true)
                .result(result)
                .messages(ctx.getMessages())
                .build())
        .build();
  }

  private String determineStatus(String message) {
    String availableStatuses =
        Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.joining(", "));

    String prompt =
        """
        You are an assistant identifying order status from user requests.
        Available statuses are: [%s]

        Rules:
        - If the user wants to see ALL orders or doesn't specify, return 'ALL'.
        - If the user specifies a status, return the exact status name from the available list.
        - If it's completely unclear what status they want (and it's not 'ALL'), return 'UNKNOWN'.

        Return ONLY the status name or 'ALL' or 'UNKNOWN'. No explanation.
        """
            .formatted(availableStatuses);

    String content = aiClient.chat(prompt, message);
    if (content == null || content.isBlank()) {
      return "ALL";
    }
    return content.trim().toUpperCase();
  }

  private OrderResultDto.OrderBriefDto toBriefDto(OrderEntity order) {
    return OrderResultDto.OrderBriefDto.builder()
        .orderId(order.getId())
        .status(order.getStatus().name())
        .totalAmount(order.getTotalAmount().toString())
        .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "")
        .build();
  }

  @Override
  public AIIntent intent() {
    return AIIntent.ORDER;
  }
}
