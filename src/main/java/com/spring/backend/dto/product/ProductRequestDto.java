package com.spring.backend.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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

  @NotNull private String type;

  @Min(1)
  @NotNull
  private Double dailyProfit;

  @NotNull private Integer quantity;

  @NotNull private Instant startedAt;

  @NotNull private Instant endAt;

  @NotNull private Long categoryId;

  private String description;

  @NotNull private Long customerId;

  @NotNull private String code;

  //    @Size(min = 4, max = 4)
  @NotNull private List<Long> imageIds;
}
