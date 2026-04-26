package com.spring.backend.adapter.s3;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.dto.UploadFileDto;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3AdapterUT {

  @Mock private S3Client s3Client;
  @Mock private S3Presigner s3Presigner;

  @InjectMocks private S3Adapter s3Adapter;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(s3Adapter, "bucketName", "test-bucket");
  }

  @Test
  @DisplayName("uploadFile should put object and return dto")
  void uploadFile_Works() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test.jpg");
    when(file.getContentType()).thenReturn("image/jpeg");
    when(file.getBytes()).thenReturn(new byte[] {1});

    PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(new URL("http://presigned.url"));
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

    UploadFileDto result = s3Adapter.uploadFile(file);

    assertThat(result.getUrl()).isEqualTo("http://presigned.url");
    assertThat(result.getKey()).contains("test.jpg");
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("getUrl should return presigned url")
  void getUrl_Works() throws Exception {
    PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(new URL("http://presigned.url"));
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

    String url = s3Adapter.getUrl("key");

    assertThat(url).isEqualTo("http://presigned.url");
  }
}
