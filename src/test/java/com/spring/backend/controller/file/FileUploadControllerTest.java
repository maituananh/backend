package com.spring.backend.controller.file;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.backend.BaseIntegrationTest;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

class FileUploadControllerTest extends BaseIntegrationTest {

  @MockBean private FileUploadService fileUploadService;

  @Test
  @WithMockUser
  void uploadFile_shouldReturnImageResponse() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

    ImageResponseDto responseDto = new ImageResponseDto();
    responseDto.setUrl("http://example.com/test.jpg");

    when(fileUploadService.upload(any())).thenReturn(responseDto);

    mockMvc.perform(multipart("/api/files/upload").file(file)).andExpect(status().isOk());
  }
}
