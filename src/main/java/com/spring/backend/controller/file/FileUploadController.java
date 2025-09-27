package com.spring.backend.controller.file;

import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadService fileUploadService;

  @PostMapping("upload")
  public ImageResponseDto uploadFile(MultipartFile file) {
    return fileUploadService.upload(file);
  }

  @GetMapping
  public void getFile() {}
}
