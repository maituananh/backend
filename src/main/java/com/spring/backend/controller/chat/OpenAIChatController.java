package com.spring.backend.controller.chat;

import com.spring.backend.dto.chat.ChatHistoryItemDto;
import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.service.AgentService;
import com.spring.backend.service.ChatHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class OpenAIChatController {

  private final AgentService agentService;
  private final ChatHistoryService chatHistoryService;
  private final UserHelper userHelper;

  @PostMapping
  public ChatResponseDto sendMessage(@RequestBody ChatRequestDto chatRequestDto) {
    return agentService.process(
        String.valueOf(userHelper.getCurrentUserId()),
        chatRequestDto.getContent(),
        chatRequestDto.getFileUrl());
  }

  @GetMapping("/history")
  public List<ChatHistoryItemDto> getHistory() {
    return chatHistoryService.getHistory(String.valueOf(userHelper.getCurrentUserId()));
  }
}
