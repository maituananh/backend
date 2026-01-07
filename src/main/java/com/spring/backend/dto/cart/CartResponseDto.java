package com.spring.backend.dto.cart;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartResponseDto {
  private Long cartId;
  private Long customerId;
  private List<CartItemResponseDto> items;
}
