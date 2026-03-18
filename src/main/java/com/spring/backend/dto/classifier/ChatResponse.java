package com.spring.backend.dto.classifier;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {

  private String status;
  private ChatData data;
  private ChatError error;
  private ChatMeta meta;

  // ── DATA ─────────────────────────────────────────────────────────────

  @Data
  @Builder
  public static class ChatData {
    private String reply;
    private String intent;
    private boolean isComplete;
    private int collectedCount;
    private int missingCount;
    private List<String> missingFields;
    private Object result;
  }

  // ── ERROR ────────────────────────────────────────────────────────────

  @Data
  @Builder
  public static class ChatError {
    private String code;
    private String message;
  }

  // ── META ─────────────────────────────────────────────────────────────

  @Data
  @Builder
  public static class ChatMeta {
    private String sessionId;
    private long responseTime;
  }

  // ── STATIC FACTORIES ─────────────────────────────────────────────────

  public static ChatResponse success(ChatData data, String sessionId, long responseTime) {
    return ChatResponse.builder()
        .status("success")
        .data(data)
        .meta(ChatMeta.builder().sessionId(sessionId).responseTime(responseTime).build())
        .build();
  }

  public static ChatResponse error(
      String code, String message, String sessionId, long responseTime) {
    return ChatResponse.builder()
        .status("error")
        .error(ChatError.builder().code(code).message(message).build())
        .meta(ChatMeta.builder().sessionId(sessionId).responseTime(responseTime).build())
        .build();
  }
}
