package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;

public abstract class AbstractChatService {

  public abstract ChatResponseDto handle(ChatRequestDto chatRequestDto);

  public abstract String description();

  public abstract CategoryAI category();
}
