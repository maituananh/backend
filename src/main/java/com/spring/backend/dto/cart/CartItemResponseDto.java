package com.spring.backend.dto.cart;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItemResponseDto {
  private Long cartItemId;
  private Long productId;
  private String productName;
  private String image;
  private BigDecimal price;
  private Integer quantity;
  private String status;
  private Boolean isActived;
}
