package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.OrderChatResponseDto;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.OrderItemRepository;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.repository.PaymentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderChatService extends AbstractChatService {

  private final ChatClient chatClient;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final PaymentRepository paymentRepository;
  private final UserHelper userHelper;

  @Override
  public ChatResponseDto handle(ChatRequestDto chatRequestDto) {
    String question = chatRequestDto.getContent();
    Long userId = userHelper.getCurrentUserId();

    // Sử dụng AI để xác định xem User đang muốn lấy đơn hàng ở trạng thái nào
    String statusStr = determineStatus(question);
    log.info("Determined status: {}", statusStr);

    List<OrderEntity> orders;
    if (statusStr == null || statusStr.equalsIgnoreCase("ALL")) {
      orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    } else {
      try {
        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
        orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
      } catch (IllegalArgumentException e) {
        orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
      }
    }

    if (orders.isEmpty()) {
      return ChatResponseDto.builder()
          .result(
              "You don't have any orders"
                  + (statusStr != null && !statusStr.equals("ALL")
                      ? " with status " + statusStr
                      : ""))
          .build();
    }

    return OrderChatResponseDto.builder()
        .result("I found " + orders.size() + " orders for you:")
        .type(String.valueOf(CategoryAI.ORDER))
        .orders(orders.stream().map(this::toBriefDto).toList())
        .build();
  }

  private String determineStatus(String question) {
    String allStatus =
        Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.joining(", "));

    String prompt =
        String.format(
            "Based on the user's question: '%s'. "
                + "Identify which order status they want to view. "
                + "Valid statuses are: [%s]. "
                + "If the user wants to see all orders or doesn't mention a specific status, return 'ALL'. "
                + "Return ONLY the status name (e.g., PENDING, CONFIRMED, CANCELLED, ALL). "
                + "Do not add any additional explanation.",
            question, allStatus);

    String content = chatClient.prompt().user(prompt).call().content();

    if (content == null || content.isBlank()) {
      return "ALL";
    }

    return content.trim().toUpperCase();
  }

  // Helper để map gọn thông tin đơn hàng trả về trong Chat
  private OrderChatResponseDto.OrderBriefDto toBriefDto(OrderEntity order) {
    return OrderChatResponseDto.OrderBriefDto.builder()
        .orderId(order.getId())
        .status(order.getStatus().name())
        .totalAmount(order.getTotalAmount().toString())
        .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "")
        .build();
  }

  @Override
  public String description() {
    return "Look up my order list (e.g., placed orders, cancelled orders)";
  }

  @Override
  public CategoryAI category() {
    return CategoryAI.ORDER;
  }
}
