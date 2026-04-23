package com.spring.backend.dto.cart;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequestDto {

  private Long productId;
  private Integer quantity;
}
