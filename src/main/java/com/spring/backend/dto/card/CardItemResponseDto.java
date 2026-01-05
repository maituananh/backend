package com.spring.backend.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardItemResponseDto {
  private Long productId;
  private String productName;
  private Double price;
  private Integer quantity;
  private String status;
}
