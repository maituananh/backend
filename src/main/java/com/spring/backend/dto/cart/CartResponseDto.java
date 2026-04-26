package com.spring.backend.dto.cart;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
  private Long cartId;
  private Long customerId;
  private List<CartItemResponseDto> items;
}
