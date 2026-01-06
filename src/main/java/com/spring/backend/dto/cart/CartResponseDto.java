package com.spring.backend.dto.cart;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartResponseDto {
  private Long cardId;
  private Long customer_id;
  private List<CartItemResponseDto> items;
}
