package com.spring.backend.controller.file;

import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadService fileUploadService;

  @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImageResponseDto uploadFile(@RequestPart("file") MultipartFile file) {
    return fileUploadService.upload(file);
  }
}
