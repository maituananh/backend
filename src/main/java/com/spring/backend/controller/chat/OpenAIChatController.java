package com.spring.backend.controller.chat;

import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class OpenAIChatController {

  private final OpenAiService openAiService;

  @PostMapping
  public String sendMessage(@RequestBody ChatRequestDto chatRequestDto) {

    openAiService.handleRequest(chatRequestDto);
    return null;
  }
}
