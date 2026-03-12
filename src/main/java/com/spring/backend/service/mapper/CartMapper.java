package com.spring.backend.service.mapper;

import com.spring.backend.dto.cart.CartItemResponseDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.CartItemEntity;

public class CartMapper {

  public static CartItemResponseDto toItemDto(CartItemEntity entity, String image) {
    return new CartItemResponseDto(
        entity.getId(),
        entity.getProduct().getId(),
        entity.getProduct().getName(),
        image,
        entity.getPrice(),
        entity.getQuantity(),
        entity.getStatus().name(),
        entity.getProduct().getIsActived());
  }

  public static CartResponseDto toCartDto(
      CartEntity cart, java.util.function.Function<CartItemEntity, String> imageFunction) {
    return new CartResponseDto(
        cart.getId(),
        cart.getCustomer().getId(),
        cart.getItems().stream().map(i -> toItemDto(i, imageFunction.apply(i))).toList());
  }
}
