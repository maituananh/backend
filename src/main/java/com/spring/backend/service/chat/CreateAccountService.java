package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAccountService extends AbstractChatService {

  @Override
  public ChatResponseDto handle(ChatRequestDto chatRequestDto) {
    return ChatResponseDto.builder()
        .result("Create account request")
        .type(String.valueOf(CategoryAI.NEW_ACCOUNT))
        .build();
  }

  @Override
  public CategoryAI category() {
    return CategoryAI.NEW_ACCOUNT;
  }

  @Override
  public String description() {
    return "Request create account by Vietnamese Citizen Identity Cards (CCCD) in this system.";
  }
}
