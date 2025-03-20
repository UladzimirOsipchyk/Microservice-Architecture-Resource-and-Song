package com.example.resourceprocessor.service;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class S3StorageService {
  private final MinioClient minioClient;

  @Value("${cloud.aws.s3.bucket-name}")
  private String bucketName;

  public S3StorageService(@Value("${cloud.aws.s3.endpoint}") String endpoint,
                          @Value("${cloud.aws.s3.access-key}") String accessKey,
                          @Value("${cloud.aws.s3.secret-key}") String secretKey) {

    this.minioClient = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }

  @SneakyThrows
  public void downloadFile(String fileName) {
    DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
        .bucket(bucketName)
        .object(fileName)
        .filename(fileName)
        .build();

    minioClient.downloadObject(downloadObjectArgs);
  }
}