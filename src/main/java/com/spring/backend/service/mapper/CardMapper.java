package com.spring.backend.service.mapper;

import com.spring.backend.dto.card.CardItemResponseDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.entity.CardEntity;
import com.spring.backend.entity.CardItemEntity;

public class CardMapper {

  public static CardItemResponseDto toItemDto(CardItemEntity entity) {
    return new CardItemResponseDto(
        entity.getProduct().getId(),
        entity.getProduct().getName(),
        entity.getPrice(),
        entity.getQuantity(),
        entity.getStatus().name());
  }

  public static CardResponseDto toCardDto(CardEntity card) {
    return new CardResponseDto(
        card.getId(),
        card.getCustomer().getId(),
        card.getItems().stream().map(CardMapper::toItemDto).toList());
  }
}
