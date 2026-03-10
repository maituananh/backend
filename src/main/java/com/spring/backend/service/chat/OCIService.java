package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import java.net.MalformedURLException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OCIService extends AbstractChatService {

  private final ChatClient chatClient;

  @Override
  public ChatResponseDto handle(ChatRequestDto chatRequestDto) {
    String response =
        chatClient
            .prompt()
            .user(
                u -> {
                  try {
                    u.text(PROMPT)
                        .media(
                            MimeTypeUtils.IMAGE_PNG,
                            URI.create(chatRequestDto.getFileUrl()).toURL());
                  } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                  }
                })
            .options(
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build())
            .call()
            .content();

    log.info("{}", response);

    return ChatResponseDto.builder().result("ok").build();
  }

  @Override
  public CategoryAI category() {
    return CategoryAI.OCI_IDENTITY;
  }

  @Override
  public String description() {
    return "Use Vietnamese Citizen Identity Cards (CCCD) to create an account.";
  }

  private static final String PROMPT =
      """
              You are an AI system specialized in extracting structured information from Vietnamese Citizen Identity Cards (CCCD).
              The image provided is the **front side of a Vietnamese Citizen Identity Card**.
              Your task is to read the text on the card and extract all visible information.

              Rules:
              * Only extract information that is clearly visible on the card.
              * Do NOT guess or fabricate missing values.
              * Preserve Vietnamese characters exactly as they appear.
              * If a field is not visible or missing, return null.
              * Return the result strictly in JSON format.
              * Do not include explanations or additional text.

              Fallback rule:
              * If Vietnamese Citizen Identity Cards (CCCD) are not recognized, return 0.

              Extract the following fields:
              {
              "id_number": "",
              "full_name": "",
              "date_of_birth": "",
              "gender": "",
              "nationality": "",
              "place_of_origin": "",
              "place_of_residence": "",
              "expiry_date": ""
              }
              """;
}
