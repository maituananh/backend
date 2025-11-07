package com.spring.backend.dto.category;

import java.time.Instant;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDto {
  private Long id;
  private String name;
  private String note;
  private Boolean isActive;
  private Instant createdAt;
  private Long createdBy;
  private Instant updatedAt;
  private Long updatedBy;
}
