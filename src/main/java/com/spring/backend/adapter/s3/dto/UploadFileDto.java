package com.spring.backend.adapter.s3.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileDto {
  private String key;
  private String url;
}
