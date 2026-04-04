package com.spring.backend.dto.product;

import com.spring.backend.enums.ProductStatus;
import java.time.LocalDate;
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
  private LocalDate startDate;
  private LocalDate endDate;
  private String image;
  private String code;
  private Boolean isActived;
  private ProductStatus status;
  private Integer availableQty;
  private Integer stockQty;
  private Double dailyProfit;
}
