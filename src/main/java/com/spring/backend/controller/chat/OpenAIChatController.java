package com.spring.backend.controller.chat;

import com.spring.backend.dto.chat.ChatCreateAccountRequestDto;
import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.service.OpenAiService;
import com.spring.backend.service.chat.OCRService;
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
  private final OCRService ocrService;

  @PostMapping
  public ChatResponseDto sendMessage(@RequestBody ChatRequestDto chatRequestDto) {
    return openAiService.handleRequest(chatRequestDto);
  }

  @PostMapping("/ocr/user")
  public ChatResponseDto ocrUser(@RequestBody ChatCreateAccountRequestDto chatRequestDto) {
    return ocrService.handle(chatRequestDto);
  }
}
