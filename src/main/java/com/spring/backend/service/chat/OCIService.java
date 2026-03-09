package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OCIService extends AbstractChatService {

  @Override
  public AbstractChatService handle(ChatRequestDto chatRequestDto) {
    return null;
  }

  @Override
  public CategoryAI category() {
    return CategoryAI.OCI_IDENTITY;
  }

  @Override
  public String description() {
    return "Using my identify card to extract information to text";
  }
}
