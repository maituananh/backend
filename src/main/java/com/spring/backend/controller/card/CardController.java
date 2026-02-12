package com.spring.backend.controller.card;

import com.spring.backend.dto.card.CardRequestDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.service.CardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CardController {
  private final CardService cardService;

  @PostMapping("/cards")
  public CardResponseDto createCard(@RequestBody CardRequestDto dto) {
    return cardService.create(dto);
  }

  @GetMapping("/users/{userId}/card-user")
  public List<CardResponseDto> getCardsByUser(@PathVariable Long userId) {
    return cardService.getByUserId(userId);
  }
}
