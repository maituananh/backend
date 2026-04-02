package com.spring.backend.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
  private Long id;

  @NotBlank private String name;

  @Min(1)
  private Double price;

  @Min(1)
  @NotNull
  private Double dailyProfit;

  @NotNull private Integer stockQty;

  @NotNull private LocalDate startDate;

  @NotNull private LocalDate endDate;

  @NotNull private Long categoryId;

  private String description;

  @NotNull private Long customerId;

  @NotNull private String code;

  @Size(min = 4, max = 4, message = "Product must have exactly 4 images")
  @NotNull
  private List<Long> imageIds;
}
