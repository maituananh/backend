package com.spring.backend.dto.card;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardResponseDto {
  private Long cardId;
  private Long customer_id;
  private List<CardItemResponseDto> items;
}
