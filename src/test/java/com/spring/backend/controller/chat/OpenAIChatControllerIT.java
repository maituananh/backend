package com.spring.backend.controller.chat;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.backend.config.BaseIntegrationTest;
import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.chat.ChatHistoryItemDto;
import com.spring.backend.dto.chat.ChatRequestDto;
import com.spring.backend.dto.chat.ChatResponseDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.AgentService;
import com.spring.backend.service.ChatHistoryService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class OpenAIChatControllerIT extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;

  @MockitoBean private AgentService agentService;
  @MockitoBean private ChatHistoryService chatHistoryService;

  private UserEntity testUser;

  @BeforeEach
  void setUp() {
    testUser =
        UserEntity.builder()
            .username("chatuser")
            .email("chatuser@example.com")
            .password("password")
            .name("Chat User")
            .phone("123456789")
            .cardId("CARD_CHAT")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();
    testUser = userRepository.save(testUser);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  private UserDetailsCustom toUserDetails(UserEntity user) {
    return UserDetailsCustom.builder()
        .id(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .build();
  }

  @Test
  void shouldSendMessage() throws Exception {
    String sessionId = String.valueOf(testUser.getId());
    String messageContent = "Hello AI";
    String fileUrl = "http://example.com/file";

    ChatRequestDto requestDto = new ChatRequestDto(messageContent, fileUrl);

    ChatResponseDto.ChatData<Object> chatData =
        ChatResponseDto.ChatData.builder().reply("Hello from AI").build();

    ChatResponseDto<Object> responseDto =
        ChatResponseDto.builder().status("success").data(chatData).build();

    when(agentService.process(sessionId, messageContent, fileUrl))
        .thenReturn((ChatResponseDto) responseDto);

    mockMvc
        .perform(
            post("/api/chat")
                .with(user(toUserDetails(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.data.reply").value("Hello from AI"));
  }

  @Test
  void shouldGetHistory() throws Exception {
    String sessionId = String.valueOf(testUser.getId());

    ChatHistoryItemDto historyItem =
        ChatHistoryItemDto.builder().userMessage("Hello AI").timestamp("some-time").build();

    when(chatHistoryService.getHistory(sessionId)).thenReturn(List.of(historyItem));

    mockMvc
        .perform(get("/api/chat/history").with(user(toUserDetails(testUser))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userMessage").value("Hello AI"));
  }
}
