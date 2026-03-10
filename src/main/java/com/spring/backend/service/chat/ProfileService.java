package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService extends AbstractChatService {

  private final UserRepository userRepository;
  private final UserHelper userHelper;

  @Override
  public ChatResponseDto handle(ChatRequestDto chatRequestDto) {
    Long currentUserId = userHelper.getCurrentUserId();
    UserEntity userEntity = userRepository.findById(currentUserId).orElse(null);

    if (userEntity == null) {
      return ChatResponseDto.builder()
          .result("You currently do not have a profile in the system.")
          .build();
    }

    return UserMapper.toProfileDto(userEntity);
  }

  @Override
  public String description() {
    return "Get my information about my profile";
  }

  @Override
  public CategoryAI category() {
    return CategoryAI.PROFILE;
  }
}
