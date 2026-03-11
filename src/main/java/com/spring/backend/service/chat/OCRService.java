package com.spring.backend.service.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.chat.ChatCreateAccountRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.OCRChatResponseDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.UserMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OCRService {

  private final ChatClient chatClient;
  private final S3Adapter s3Adapter;
  private final ObjectMapper objectMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @Transactional
  public ChatResponseDto handle(ChatCreateAccountRequestDto chatRequestDto) {
    if (!chatRequestDto.validateCreateAccountRequestDto()) {
      return ChatResponseDto.errorAnswer("Missing data");
    }

    if (userRepository.existsByUsername(chatRequestDto.getUsername())) {
      return ChatResponseDto.errorAnswer("Username already exists");
    }

    if (userRepository.existsByPhone(chatRequestDto.getPhone())) {
      return ChatResponseDto.errorAnswer("Phone number already exists");
    }

    if (userRepository.existsByEmail(chatRequestDto.getEmail())) {
      return ChatResponseDto.errorAnswer("Email already exists");
    }

    if (chatRequestDto.getFileUrl() == null || chatRequestDto.getFileUrl().isEmpty()) {
      return ChatResponseDto.errorAnswer("Vietnamese Citizen Identity Card");
    }

    String response = analyticsImage(chatRequestDto.getFileUrl());

    log.info("{}", response);

    if (response == null || response.contains("error")) {
      return ChatResponseDto.errorAnswer(response);
    }

    CitizenIdResponse citizenIdResponse;
    try {
      citizenIdResponse = objectMapper.readValue(response, CitizenIdResponse.class);
    } catch (JsonProcessingException e) {
      return ChatResponseDto.errorAnswer("System error");
    }

    if (citizenIdResponse.getIdNumber() != null
        && userRepository.existsByCardId(citizenIdResponse.getIdNumber())) {
      return ChatResponseDto.errorAnswer("Vietnamese Citizen Identity Card already registered");
    }

    String password = String.valueOf(UUID.randomUUID());
    String passwordHash = passwordEncoder.encode(password);

    UserEntity entity = UserMapper.toEntity(citizenIdResponse, chatRequestDto, passwordHash);
    userRepository.save(entity);

    return OCRChatResponseDto.builder()
        .username(chatRequestDto.getUsername())
        .password(password)
        .result("username: %s | password: %s".formatted(chatRequestDto.getUsername(), passwordHash))
        .build();
  }

  private String analyticsImage(String fileUrl) {
    return chatClient
        .prompt()
        .user(
            u -> {
              try {
                URL imageUrl = URI.create(s3Adapter.getUrl(fileUrl)).toURL();
                byte[] imageBytes = imageUrl.openStream().readAllBytes();
                Resource imageResource = new ByteArrayResource(imageBytes);

                u.text(PROMPT).media(MimeTypeUtils.IMAGE_PNG, imageResource);
              } catch (MalformedURLException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .options(OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build())
        .call()
        .content();
  }

  public CategoryAI category() {
    return CategoryAI.OCR_IDENTITY;
  }

  private static final String PROMPT =
      """
          You are an OCR specialist for Vietnamese Citizen Identity Cards (CCCD - Căn cước công dân).

          ## INPUT
          You will receive an image of the FRONT side of a Vietnamese CCCD.

          ## TASK
          Extract all visible text fields from the card accurately.

          ## STRICT RULES
          - Return ONLY a raw JSON object. No markdown, no code blocks, no explanations.
          - Preserve exact Vietnamese characters and diacritics as printed on the card.
          - Do NOT guess, infer, or fabricate any value.
          - If a field is unclear or not present, set it to null.
          - Date format must be: DD/MM/YYYY

          ## VALIDATION
          - If the image is NOT a Vietnamese CCCD front side, return exactly: {"error": "NOT_CCCD"}
          - If the image is unreadable or too blurry, return exactly: {"error": "UNREADABLE"}

          ## OUTPUT FORMAT
          Return exactly this JSON structure:
          {
            "id_number": "string or null",
            "full_name": "string or null",
            "date_of_birth": "DD/MM/YYYY or null",
            "gender": "Nam | Nữ | null",
            "nationality": "string or null",
            "place_of_origin": "string or null",
            "place_of_residence": "string or null",
            "expiry_date": "DD/MM/YYYY or null"
          }
          """;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CitizenIdResponse {

    @JsonProperty("id_number")
    private String idNumber;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("place_of_origin")
    private String placeOfOrigin;

    @JsonProperty("place_of_residence")
    private String placeOfResidence;
  }
}
