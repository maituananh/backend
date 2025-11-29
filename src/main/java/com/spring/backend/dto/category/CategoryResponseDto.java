package com.spring.backend.dto.category;

import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.CategoryEntity;
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

  public CategoryResponseDto(CategoryEntity entity) {
    this.id = entity.getId();
    this.name = entity.getName();
    this.note = entity.getNote();
    this.isActive = entity.getIsActive();
    this.createdAt = entity.getCreatedAt();
    this.createdBy = entity.getCreatedBy();
    //  this.createdByUser = entity.getCreatedByUser();
    this.updatedAt = entity.getUpdatedAt();
    this.updatedBy = entity.getUpdatedBy();
  }
}
