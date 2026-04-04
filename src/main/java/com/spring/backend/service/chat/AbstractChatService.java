package com.spring.backend.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.enums.AgentAIStep;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractChatService {

  public abstract ChatResponseDto<?> process(AgentContext ctx, String message, String fileUrl);

  public abstract AIIntent intent();

  protected AgentContext parseAndUpdate(AgentContext ctx, String aiReply) {
    try {
      JsonNode root = new ObjectMapper().readTree(aiReply);

      if (root.has("error")) {
        log.warn("AI returned error: {}", root.get("error").asText());
        ctx.setStep(AgentAIStep.IN_PROGRESS);
        return ctx;
      }

      String status = root.get("status").asText();
      JsonNode extracted = root.get("extracted");

      if (extracted != null && extracted.isObject()) {
        extracted
            .properties()
            .forEach(
                entry -> {
                  String val = entry.getValue().isNull() ? null : entry.getValue().asText();
                  ctx.getCollectedParams().put(entry.getKey(), val);
                });
      }

      List<String> missing = new ArrayList<>();
      if (root.has("missing_fields") && root.get("missing_fields").isArray()) {
        root.get("missing_fields").forEach(f -> missing.add(f.asText()));
      }
      ctx.setMissingFields(String.join(", ", missing));

      try {
        ctx.setCollectedJson(new ObjectMapper().writeValueAsString(ctx.getCollectedParams()));
      } catch (Exception e) {
        log.warn("Failed to stringify collectedParams", e);
      }

      ctx.setStep("COMPLETE".equals(status) ? AgentAIStep.COMPLETED : AgentAIStep.IN_PROGRESS);

    } catch (Exception e) {
      log.error("Failed to parse AI reply: {}", aiReply, e);
      ctx.setStep(AgentAIStep.IN_PROGRESS);
    }

    ctx.setUpdatedAt(Instant.now().toString());
    return ctx;
  }

  protected String cleanReply(String aiReply) {
    if (aiReply == null) return "";
    if (aiReply.trim().startsWith("{")) {
      try {
        JsonNode root = new ObjectMapper().readTree(aiReply);
        if (root.has("message")) return root.get("message").asText();
        if (root.has("error")) {
          String error = root.get("error").asText();
          return switch (error) {
            case "NOT_CCCD" ->
                "The image you provided doesn't look like a Vietnamese Citizen ID card. Please try again with a clear photo.";
            default -> "AI reported an error: " + error;
          };
        }
      } catch (Exception e) {
        log.warn("Failed to parse JSON reply message: {}", aiReply);
      }
    }
    if (aiReply.startsWith("COMPLETE|")) return "Information collected. Processing...";
    if (aiReply.startsWith("PARTIAL|")) {
      String[] parts = aiReply.split("\\|", 3);
      return parts.length == 3 ? parts[2].trim() : aiReply;
    }
    return aiReply;
  }
}
