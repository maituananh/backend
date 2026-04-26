package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.controller.chat.OpenAIChatController;
import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.service.AgentService;
import com.spring.backend.service.ChatHistoryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OpenAIChatControllerUT {

  private MockMvc mockMvc;

  @Mock private AgentService agentService;
  @Mock private ChatHistoryService chatHistoryService;
  @Mock private UserHelper userHelper;

  @InjectMocks private OpenAIChatController openAIChatController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(openAIChatController).build();
  }

  @Test
  @DisplayName("sendMessage should return response")
  void sendMessage_Works() throws Exception {
    ChatRequestDto request = new ChatRequestDto();
    request.setContent("hello");

    when(userHelper.getCurrentUserId()).thenReturn(10L);

    ChatResponseDto response =
        ChatResponseDto.builder()
            .data(ChatResponseDto.ChatData.builder().reply("hi").build())
            .build();
    doReturn(response).when(agentService).process(anyString(), anyString(), any());

    mockMvc
        .perform(
            post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getHistory should return list")
  void getHistory_Works() throws Exception {
    when(userHelper.getCurrentUserId()).thenReturn(10L);
    when(chatHistoryService.getHistory("10")).thenReturn(List.of());

    mockMvc.perform(get("/api/chat/history")).andExpect(status().isOk());
  }
}
