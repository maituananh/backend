package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.card.CardRequestDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.infrastructure.entity.CardEntity;
import com.spring.backend.infrastructure.entity.UserEntity;
import com.spring.backend.infrastructure.repository.CardJpaRepository;
import com.spring.backend.infrastructure.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardServiceUT {

  @Mock private CardJpaRepository cardRepository;
  @Mock private UserJpaRepository userRepository;
  @Mock private UserHelper userHelper;

  @InjectMocks private CardService cardService;

  @Test
  @DisplayName("create should save card and return dto")
  void create_Works() {
    CardRequestDto dto = CardRequestDto.builder().numberCard("1234").build();
    Long userId = 1L;
    UserEntity user = new UserEntity();
    user.setId(userId);

    CardEntity saved = new CardEntity();
    saved.setId(100L);
    saved.setNumberOfCard("1234");

    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(cardRepository.save(any())).thenReturn(saved);

    CardResponseDto result = cardService.create(dto);

    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getNumberCard()).isEqualTo("1234");
  }

  @Test
  @DisplayName("getMyCards should return user cards")
  void getMyCards_Works() {
    Long userId = 1L;
    CardEntity card = new CardEntity();
    card.setId(100L);
    card.setNumberOfCard("1234");

    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(cardRepository.findByCustomerId(userId)).thenReturn(List.of(card));

    List<CardResponseDto> result = cardService.getMyCards();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(100L);
  }
}
