package com.spring.backend.controller.card;

import com.spring.backend.dto.card.AddToCardRequestDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

  private final CardService cardService;

  @PostMapping("/{customerId}/items")
  public CardResponseDto addToCard(
      @PathVariable Long customerId, @Valid @RequestBody AddToCardRequestDto dto) {
    return cardService.addToCard(customerId, dto);
  }

  @GetMapping("/{customerId}")
  public CardResponseDto getMyCard(@PathVariable Long customerId) {
    return cardService.getMyCard(customerId);
  }
}
