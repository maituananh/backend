package com.spring.backend.service.chat;

import com.spring.backend.dto.chat.ChatRequestDto;

public abstract class AbstractChatService {

  public abstract AbstractChatService handle(ChatRequestDto chatRequestDto);

  public abstract String description();

  public abstract CategoryAI category();
}
