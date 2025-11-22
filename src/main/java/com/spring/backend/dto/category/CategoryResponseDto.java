package com.spring.backend.dto.category;

import com.spring.backend.dto.user.UserDto;
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
  private UserDto createdByUser;
  private Instant updatedAt;
  private Long updatedBy;
}
