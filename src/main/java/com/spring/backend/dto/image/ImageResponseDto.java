package com.spring.backend.dto.image;

import com.spring.backend.entity.ImageEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDto {

  private Long id;
  private String url;

  public ImageResponseDto(ImageEntity imageEntity) {
    this.id = imageEntity.getId();
    this.url = imageEntity.getFileName();
  }
}
