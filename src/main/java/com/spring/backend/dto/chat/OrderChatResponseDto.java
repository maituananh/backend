package com.spring.backend.dto.chat;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OrderChatResponseDto extends ChatResponseDto {

  private List<OrderBriefDto> orders;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderBriefDto {
    private Long orderId;
    private String status;
    private String totalAmount;
    private String createdAt;
  }
}
