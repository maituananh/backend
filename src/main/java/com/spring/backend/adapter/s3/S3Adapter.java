package com.spring.backend.adapter.s3;

import com.spring.backend.adapter.s3.dto.UploadFileDto;
import com.spring.backend.utils.FileUtils;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3Adapter {

  @Value("${aws.s3.bucket}")
  private String bucketName;

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @SneakyThrows
  public UploadFileDto uploadFile(MultipartFile multipartFile) {
    String key = FileUtils.createNewName(multipartFile.getOriginalFilename());

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(multipartFile.getContentType())
            .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

    return UploadFileDto.builder().url(getUrl(key)).key(key).build();
  }

  public String getUrl(String valueKey) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(valueKey).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofDays(1))
            .getObjectRequest(getObjectRequest)
            .build();

    return s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
  }
}
