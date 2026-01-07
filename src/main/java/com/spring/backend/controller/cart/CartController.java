package com.spring.backend.controller.cart;

import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @PostMapping
  public CartResponseDto addToCart(@Valid @RequestBody AddToCartRequestDto dto) {
    return cartService.addToCart(dto);
  }

  @GetMapping
  public CartResponseDto getCartByCustomerId() {
    return cartService.getCartByCustomerId();
  }
}
