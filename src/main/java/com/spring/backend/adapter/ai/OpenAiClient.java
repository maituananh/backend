package com.spring.backend.adapter.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.service.AIClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiClient implements AIClient {

  private final ChatClient chatClient;
  private final S3Adapter s3Adapter;

  // ── CLASSIFY ─────────────────────────────────────────────────────────

  @Override
  public String classify(String message) {
    String systemPrompt =
        """
                Classify the user intent. Return JSON only, no explanation, no markdown.
                Intents: CREATE_ACCOUNT | PROFILE | ORDER | OTHER
                Format: {"intent":"<INTENT>","confidence":<0.0-1.0>}
                """;

    String raw = call(systemPrompt, message);

    try {
      JsonNode node = new ObjectMapper().readTree(raw);
      String intent = node.get("intent").asText("OTHER");
      double confidence = node.get("confidence").asDouble(0);
      log.debug("Classified intent={} confidence={}", intent, confidence);
      return intent;
    } catch (Exception e) {
      log.warn("Failed to parse classify response: {}", raw);
      return "OTHER";
    }
  }

  // ── CHAT ─────────────────────────────────────────────────────────────

  @Override
  public String chat(String systemPrompt, String message) {
    return call(systemPrompt, message);
  }

  @Override
  public String chat(String systemPrompt, String message, String fileUrl) {
    try {
      URL imageUrl = URI.create(s3Adapter.getUrl(fileUrl)).toURL();
      byte[] imageBytes = imageUrl.openStream().readAllBytes();
      Resource imageResource = new ByteArrayResource(imageBytes);

      return call(systemPrompt, message, imageResource);
    } catch (MalformedURLException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ── INTERNAL ─────────────────────────────────────────────────────────

  private String call(String systemPrompt, String userMessage) {
    try {
      return chatClient.prompt().system(systemPrompt).user(userMessage).call().content();
    } catch (Exception e) {
      log.error("OpenAI ChatClient call failed", e);
      throw new RuntimeException("AI service unavailable: " + e.getMessage());
    }
  }

  private String call(String systemPrompt, String userMessage, Resource imageResource) {
    return chatClient
        .prompt()
        .system(systemPrompt)
        .user(u -> u.text(userMessage).media(MimeTypeUtils.IMAGE_JPEG, imageResource))
        .call()
        .content();
  }
}
