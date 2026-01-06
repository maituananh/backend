package com.spring.backend.controller.cart;

import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CartController {

  private final CartService cardService;

  @PostMapping
  public CartResponseDto addToCard(@Valid @RequestBody AddToCartRequestDto dto) {
    return cardService.addToCard(dto);
  }

  @GetMapping("/{customerId}")
  public CartResponseDto getMyCard(@PathVariable Long customerId) {
    return cardService.getMyCard(customerId);
  }
}
