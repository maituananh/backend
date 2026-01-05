package com.spring.backend.dto.card;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCardRequestDto {

  private Long productId;
  private Integer quantity;
}
