package com.spring.backend.service;

import com.spring.backend.dto.card.CardRequestDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.entity.CardEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.CardRepository;
import com.spring.backend.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

  private final CardRepository cardRepository;
  private final UserRepository userRepository;
  private final UserHelper userHelper;

  @Transactional
  public CardResponseDto create(CardRequestDto dto) {

    Long userId = userHelper.getCurrentUserId();

    UserEntity user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    CardEntity entity =
        CardEntity.builder().numberOfCard(dto.getNumberCard()).customer(user).build();

    CardEntity saved = cardRepository.save(entity);

    return CardResponseDto.builder().id(saved.getId()).numberCard(saved.getNumberOfCard()).build();
  }

  public List<CardResponseDto> getMyCards() {

    Long userId = userHelper.getCurrentUserId();

    return cardRepository.findByCustomerId(userId).stream()
        .map(
            card ->
                CardResponseDto.builder()
                    .id(card.getId())
                    .numberCard(card.getNumberOfCard())
                    .build())
        .toList();
  }
}
