package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService extends AbstractChatService {

  private final UserRepository userRepository;
  private final UserHelper userHelper;

  @Override
  public AbstractChatService handle(ChatRequestDto chatRequestDto) {
    Long currentUserId = userHelper.getCurrentUserId();
    UserEntity userEntity = userRepository.findById(currentUserId).orElse(null);

    return null;
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
