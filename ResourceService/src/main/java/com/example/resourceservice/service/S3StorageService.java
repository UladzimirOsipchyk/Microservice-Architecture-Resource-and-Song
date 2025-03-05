package com.example.resourceservice.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
  public String uploadFile(String fileName, byte[] fileData, String contentType) {
    InputStream inputStream = new ByteArrayInputStream(fileData);
    PutObjectArgs objectArgs = PutObjectArgs.builder()
        .bucket(bucketName)
        .object(fileName)
        .stream(inputStream, fileData.length, -1)
        .contentType(contentType)
        .build();

    minioClient.putObject(objectArgs);

    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucketName)
            .object(fileName)
            .expiry(1, TimeUnit.DAYS)
            .build()
    );
  }
}
