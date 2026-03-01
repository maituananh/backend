package com.spring.backend.dto.product;

import com.spring.backend.dto.category.CategoryResponseDto;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.dto.user.UserDto;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponseDto {
  private Long id;
  private String name;
  private Double price;
  private LocalDate startDate;
  private LocalDate endDate;
  private String type;
  private String code;
  private List<ImageResponseDto> images;
  private Integer quantity;
  private CategoryResponseDto category;
  private UserDto user;
  private Double dailyProfit;
}
