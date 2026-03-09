package com.spring.backend.service;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.service.chat.AbstractChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

  private final List<AbstractChatService> handlers;
  private final ChatClient chatClient;

  public void handleRequest(ChatRequestDto chatRequestDto) {
    String category = classificationQuestions(chatRequestDto.getContent());

    if (category.equals("0")) {
      throw new RuntimeException("Sorry your question is not support !!");
    }

    for (AbstractChatService handler : handlers) {
      if (handler.category().name().equalsIgnoreCase(category)) {
        handler.handle(chatRequestDto);
      }
    }
  }

  private String classificationQuestions(String userInput) {
    String response =
        chatClient
            .prompt()
            .system(
                """
                You are an intelligent AI assistant that performs request classification.
                Your task is to classify the user's request into one of the predefined categories.

                Available categories:
                %s

                Instructions:
                - Analyze the user's input carefully.
                - Use the description to understand what each category means.
                - Select the single best matching category.

                Strict rules for the response:
                - You MUST return ONLY the category name (the text before the colon).
                - Do NOT include the description.
                - Do NOT include the colon.
                - Do NOT add explanations, punctuation, quotes, or extra spaces.
                - The response must match the category name character by character.

                Fallback rule:
                - If the user's request does not match ANY category, return exactly: 0

                Output:
                - Return exactly one category name from the list OR 0
                """
                    .formatted(getCategories()))
            .user(userInput)
            .call()
            .content();

    return response.trim();
  }

  private String getCategories() {
    StringBuilder categories = new StringBuilder();

    for (AbstractChatService handler : handlers) {
      categories.append(handler.category()).append(": ").append(handler.description()).append("/n");
    }

    return categories.toString();
  }
}
