package com.spring.backend.dto.card;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardResponseDto {
  private Long id;
  private String numberCard;
}
