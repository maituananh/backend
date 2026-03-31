package com.spring.backend.dto.product;

import com.spring.backend.enums.ProductStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class ProductSearchDto {
  private Integer page;
  private Integer size;
  private String name;
  private ProductStatus status;
  private Double price;
  private Long customerId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private String code;
  private List<Integer> categoryIds;
}
