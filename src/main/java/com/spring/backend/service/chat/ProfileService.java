package com.spring.backend.service.chat;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.dto.chat.ProfileChatResponseDto;
import com.spring.backend.dto.chat.ProfileResultDto;
import com.spring.backend.dto.classifier.AgentContext;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.AgentAIStep;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.AIClient;
import com.spring.backend.service.mapper.UserMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService extends AbstractChatService {

  private final UserRepository userRepository;
  private final UserHelper userHelper;
  private final S3Adapter s3Adapter;
  private final AIClient aiClient;

  @Override
  public ChatResponseDto<?> process(AgentContext ctx, String message, String fileUrl) {
    Long currentUserId = userHelper.getCurrentUserId();
    UserEntity userEntity = userRepository.findById(currentUserId).orElse(null);

    if (userEntity == null) {
      return ChatResponseDto.errorAnswer("You currently do not have a profile in the system.");
    }

    String prompt = buildPrompt(ctx, userEntity);
    String aiReply = aiClient.chat(prompt, message);

    parseAndUpdate(ctx, aiReply);
    updateUserEntity(userEntity, ctx.getCollectedParams());
    userRepository.save(userEntity);

    ctx.setStep(AgentAIStep.COMPLETED);

    ProfileResultDto profile = UserMapper.toProfileDto(userEntity, s3Adapter);

    return ProfileChatResponseDto.builder()
        .status("success")
        .data(
            ChatResponseDto.ChatData.<ProfileResultDto>builder()
                .reply(cleanReply(aiReply))
                .intent(ctx.getIntent())
                .isComplete(true)
                .result(profile)
                .messages(ctx.getMessages())
                .build())
        .build();
  }

  private void updateUserEntity(UserEntity user, Map<String, String> data) {
    if (data.containsKey("age") && data.get("age") != null) {
      try {
        user.setAge(Integer.parseInt(data.get("age")));
      } catch (Exception ignored) {
      }
    }
    if (data.containsKey("full_name") && data.get("full_name") != null) {
      user.setName(data.get("full_name"));
    }
    if (data.containsKey("phone") && data.get("phone") != null) {
      user.setPhone(data.get("phone"));
    }
    if (data.containsKey("email") && data.get("email") != null) {
      user.setEmail(data.get("email"));
    }
    if (data.containsKey("address") && data.get("address") != null) {
      user.setAddress(data.get("address"));
    }
    if (data.containsKey("gender") && data.get("gender") != null) {
      user.setGender(data.get("gender"));
    }
  }

  private String buildPrompt(AgentContext ctx, UserEntity user) {
    return """
        You are a Profile Assistant.
        The user is viewing their profile. They might want to update some missing information.

        ## USER CURRENT PROFILE
        - Full Name: %s
        - Age: %s
        - Gender: %s
        - Phone: %s
        - Email: %s
        - Address: %s

        ## TASK
        1. If the user provides new information (like "I am 25 years old"), extract it.
        2. Return a JSON structure.

        ## OUTPUT FORMAT
        {
          "status": "COMPLETE",
          "extracted": {
            "full_name": "string or null",
            "age": "string or null",
            "gender": "string or null",
            "phone": "string or null",
            "email": "string or null",
            "address": "string or null"
          },
          "message": "Assistant's polite response in Vietnamese. Mention what was updated if applicable."
        }
        """
        .formatted(
            user.getName(),
            user.getAge(),
            user.getGender(),
            user.getPhone(),
            user.getEmail(),
            user.getAddress());
  }

  @Override
  public AIIntent intent() {
    return AIIntent.PROFILE;
  }
}
