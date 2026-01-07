package com.spring.backend.service.mapper;

import com.spring.backend.dto.cart.CartItemResponseDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.CartItemEntity;

public class CartMapper {

  public static CartItemResponseDto toItemDto(CartItemEntity entity) {
    return new CartItemResponseDto(
        entity.getProduct().getId(),
        entity.getProduct().getName(),
        entity.getPrice(),
        entity.getQuantity(),
        entity.getStatus().name());
  }

  public static CartResponseDto toCartDto(CartEntity cart) {
    return new CartResponseDto(
        cart.getId(),
        cart.getCustomer().getId(),
        cart.getItems().stream().map(CartMapper::toItemDto).toList());
  }
}
