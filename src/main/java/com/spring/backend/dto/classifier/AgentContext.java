package com.spring.backend.dto.classifier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.enums.AgentAIStep;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentContext {
  private String sessionId;
  private String intent; // CREATE_ACCOUNT | RESET_PASSWORD | ...
  private AgentAIStep step; // "IN_PROGRESS" | "COMPLETE" | "CANCELLED"
  private int stepTotal; // tổng số bước của flow này

  private Map<String, String> collectedParams; // params đã thu thập
  private String missingFields; // fields còn thiếu, cách nhau bởi dấu phẩy
  private String collectedJson; // gom lại tất cả fields đã đọc được

  private String createdAt;
  private String updatedAt;

  private List<ConversationMessage> messages;

  // Helper
  public boolean isComplete() {
    return AgentAIStep.COMPLETED == this.step;
  }

  public String getCollectedJson() {
    if (collectedParams == null || collectedParams.isEmpty()) return "{}";
    try {
      return new ObjectMapper().writeValueAsString(collectedParams);
    } catch (Exception e) {
      return "{}";
    }
  }
}
