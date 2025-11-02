package com.spring.backend.service;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.adapter.s3.dto.UploadFileDto;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileUploadService {

  private final ImageRepository imageRepository;
  private final S3Adapter s3Adapter;

  @Transactional
  public ImageResponseDto upload(final MultipartFile file) {
    UploadFileDto fileMetadata = s3Adapter.uploadFile(file);

    ImageEntity imageEntity =
        imageRepository.save(ImageEntity.builder().fileName(fileMetadata.getKey()).build());

    return new ImageResponseDto(imageEntity);
  }
}
