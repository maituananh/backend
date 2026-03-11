package com.spring.backend.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItemResponseDto {
  private Long productId;
  private String productName;
  private String image;
  private Double price;
  private Integer quantity;
  private String status;
  private Boolean isActived;
}
