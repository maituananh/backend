package com.spring.backend.dto.product;

import java.time.Instant;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
  private Long id;
  private String name;
  private String code;
  private String type;
  private double price;
  private Double dailyProfit;
  private int quantity;
  private Instant startDay;
  private Instant endDate;
  private String description;
  private Long categoryId;
  private Long customerId;
  private List<Long> imageIds;

  private Instant createdAt;
  private Long createdBy;
  private Instant updatedAt;
  private Long updatedBy;
}
