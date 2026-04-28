package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.adapter.s3.dto.UploadFileDto;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceUT {

  @Mock private ImageRepository imageRepository;
  @Mock private S3Adapter s3Adapter;

  @InjectMocks private FileUploadService fileUploadService;

  @Test
  @DisplayName("upload should upload to S3 and save to repo")
  void upload_Works() {
    MultipartFile file = mock(MultipartFile.class);
    UploadFileDto s3Result = new UploadFileDto();
    s3Result.setKey("key");

    ImageEntity entity = new ImageEntity();
    entity.setId(1L);
    entity.setFileName("key");

    when(s3Adapter.uploadFile(file)).thenReturn(s3Result);
    when(imageRepository.save(any())).thenReturn(entity);

    ImageResponseDto result = fileUploadService.upload(file);

    assertThat(result.getId()).isEqualTo(1L);
    verify(s3Adapter).uploadFile(file);
    verify(imageRepository).save(any());
  }
}
