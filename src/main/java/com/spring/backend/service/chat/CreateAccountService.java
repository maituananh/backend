package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.CreateAccountChatResponseDto;
import com.spring.backend.dto.chat.CreateAccountResultDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.AIClient;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAccountService extends AbstractChatService {

  private final AIClient aiClient;
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public ChatResponseDto<?> process(AgentContext ctx, String message, String fileUrl) {
    String prompt = buildPrompt(ctx);
    String aiReply =
        fileUrl == null ? aiClient.chat(prompt, message) : aiClient.chat(prompt, message, fileUrl);

    // Process AI response & update context
    parseAndUpdate(ctx, aiReply);

    CreateAccountResultDto result =
        CreateAccountResultDto.builder()
            .extracted(ctx.getCollectedParams())
            .missingFields(Arrays.asList(ctx.getMissingFields().split(",\\s*")))
            .build();

    var dto =
        CreateAccountChatResponseDto.builder()
            .status("success")
            .data(
                ChatResponseDto.ChatData.<CreateAccountResultDto>builder()
                    .reply(cleanReply(aiReply))
                    .intent(ctx.getIntent())
                    .isComplete(ctx.isComplete())
                    .result(result)
                    .messages(ctx.getMessages())
                    .build())
            .build();

    if (dto.getData().isComplete()) {
      handleAccountCreation(ctx, result, dto);
    }

    return dto;
  }

  private void handleAccountCreation(
      AgentContext ctx, CreateAccountResultDto result, CreateAccountChatResponseDto dto) {
    Map<String, String> data = ctx.getCollectedParams();
    String username = data.get("username");
    String email = data.get("email");
    String cardId = data.get("id_number");

    // 1. Validation
    StringBuilder errorMsg = new StringBuilder();
    if (userRepository.existsByUsername(username)) {
      errorMsg.append("Username '").append(username).append("' already exists. ");
    }
    if (userRepository.existsByEmail(email)) {
      errorMsg.append("Email '").append(email).append("' already exists. ");
    }
    if (userRepository.existsByCardId(cardId)) {
      errorMsg
          .append("Citizen ID '")
          .append(cardId)
          .append("' is already registered to another account. ");
    }

    if (errorMsg.length() > 0) {
      dto.getData().setReply(errorMsg + "Please check your information again.");
      dto.getData().setComplete(true);
      ctx.setStep(AgentAIStep.COMPLETED);
      return;
    }

    // 2. Create and Save
    String password = UUID.randomUUID().toString();
    UserEntity user = mapToUserEntity(data, password);
    userRepository.save(user);

    // 3. Update Result
    result.setUsername(user.getUsername());
    result.setPassword(password);
    result.setExtracted(null);
  }

  private UserEntity mapToUserEntity(Map<String, String> data, String rawPassword) {
    UserEntity user =
        UserEntity.builder()
            .username(data.get("username"))
            .password(passwordEncoder.encode(rawPassword))
            .name(data.get("full_name"))
            .email(data.get("email"))
            .phone(data.get("phone"))
            .cardId(data.get("id_number"))
            .address(data.get("place_of_residence"))
            .gender(data.get("gender"))
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();

    String dob = data.get("date_of_birth");
    if (dob != null && !dob.isEmpty() && !"null".equalsIgnoreCase(dob)) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate birthDate = LocalDate.parse(dob, formatter);
        user.setBirthDate(birthDate);
        user.setAge(Period.between(birthDate, LocalDate.now()).getYears());
      } catch (Exception e) {
        log.warn("Failed to parse date of birth: {}", dob);
      }
    }
    return user;
  }

  @Override
  public AIIntent intent() {
    return AIIntent.CREATE_ACCOUNT;
  }

  private String buildPrompt(AgentContext ctx) {
    return """
            You are an intelligent account creation assistant for an internal enterprise system.

            ## CONTEXT
            You are helping to create a new employee account.
            Information will come from 2 sources:
            - A Vietnamese Citizen Identity Card (CCCD) image
            - Additional text input from the user (username, phone, email)

            ## REQUIRED FIELDS
            From CCCD image:
            - full_name          : Full name as printed on card
            - date_of_birth      : Date of birth (DD/MM/YYYY)
            - gender             : Male | Female
            - id_number          : Citizen ID number (12 digits)
            - place_of_origin    : Place of origin
            - place_of_residence : Place of residence

            From user text input:
            - username           : Login username (lowercase, no spaces, no special chars)
            - phone              : phone number
            - email              : Work email

            ## COLLECTED SO FAR
            %s

            ## TASK
            1. If CCCD image is provided → extract all 7 fields from image.
            2. Extract username, phone, email from user text if present.
            3. Merge with COLLECTED SO FAR — do NOT re-ask already collected fields.
            4. Determine what is still missing.

            ## STRICT RULES
            - Return ONLY a raw JSON object. No markdown, no code blocks, no explanations.
            - Preserve exact Vietnamese characters and diacritics as printed on card (especially for names and addresses).
            - Do NOT guess, infer, or fabricate any value.
            - If a field cannot be extracted, set it to null.
            - Date format must be: DD/MM/YYYY
            - Username must be lowercase, no spaces (e.g. "nguyenvana").
            - id_number must be exactly 12 digits.

            ## VALIDATION
            - If image is provided but NOT a Vietnamese CCCD → return: {"error": "NOT_CCCD"}

            ## OUTPUT FORMAT
            Return exactly this JSON structure:
            {
              "status": "PARTIAL | COMPLETE",
              "extracted": {
                "full_name":          "string or null",
                "date_of_birth":      "DD/MM/YYYY or null",
                "gender":             "Male | Female | null",
                "id_number":          "string or null",
                "place_of_origin":    "string or null",
                "place_of_residence": "string or null",
                "username":           "string or null",
                "phone":              "string or null",
                "email":              "string or null"
              },
              "missing_fields": ["field1", "field2"] or [],
              "message": "string"
            }

            Where:
            - status        : PARTIAL if missing_fields not empty, COMPLETE if empty
            - missing_fields: remaining fields not yet collected, [] if all done
            - message       : if PARTIAL → Confirm what's already there + ask about missing fields in English
                              if COMPLETE → Confirm account creation was successful in English.
            """
        .formatted(ctx.getCollectedJson());
  }
}
