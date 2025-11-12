package com.spring.backend.dto.product;

import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
  private Long id;
  private String name;
  private Double price;
  private Instant startDay;
  private Instant endDate;
  private String type;
  private String image;
  private String code;
}
