package com.spring.backend.dto.chat;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResultDto {
  private String message;
  private List<OrderBriefDto> orders;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderBriefDto {
    private Long orderId;
    private String status;
    private String totalAmount;
    private String createdAt;
  }
}
