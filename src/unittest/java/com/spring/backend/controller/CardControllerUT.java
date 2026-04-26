package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.backend.controller.card.CardController;
import com.spring.backend.dto.card.CardRequestDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.service.CardService;
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
class CardControllerUT {

  private MockMvc mockMvc;

  @Mock private CardService cardService;

  @InjectMocks private CardController cardController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
  }

  @Test
  @DisplayName("create card should return ok")
  void create_Works() throws Exception {
    CardRequestDto request = CardRequestDto.builder().numberCard("1234").build();
    when(cardService.create(any())).thenReturn(CardResponseDto.builder().id(1L).build());

    mockMvc
        .perform(
            post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getMyCards should return ok")
  void getMyCards_Works() throws Exception {
    when(cardService.getMyCards()).thenReturn(List.of());

    mockMvc.perform(get("/api/cards")).andExpect(status().isOk());
  }
}
