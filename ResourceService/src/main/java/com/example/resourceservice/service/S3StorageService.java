package com.example.resourceservice.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
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

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class S3StorageService {
  private final MinioClient minioClient;

  @Value("${cloud.aws.s3.bucket-name}")
  private String bucketName;

  public S3StorageService(@Value("${cloud.aws.s3.endpoint}") String endpoint,
                          @Value("${cloud.aws.s3.access-key}") String accessKey,
                          @Value("${cloud.aws.s3.secret-key}") String secretKey) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    this.minioClient = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }

  @SneakyThrows
  public String uploadFile(String fileName, byte[] fileData, String contentType) {
    initBucketIfNotExists();

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

  @SneakyThrows
  public File downloadFile(String fileName) {

    DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
        .bucket(bucketName)
        .object(fileName)
        .filename(fileName)
        .build();

    minioClient.downloadObject(downloadObjectArgs);

    return new File(downloadObjectArgs.filename());
  }

  @SneakyThrows
  public void removeFile(String fileName) {

    RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
        .bucket(bucketName)
        .object(fileName)
        .build();

    minioClient.removeObject(removeObjectArgs);
  }

  @SneakyThrows
  @Transactional
  public void removeFiles(List<String> fileNamesList) {
    System.out.println("remove files from minio...");
    List<DeleteObject> listToDelete = new LinkedList<>();

    fileNamesList.forEach(it -> listToDelete.add(new DeleteObject(it)));

    System.out.println("Files to remove from Minio: " + listToDelete);

    RemoveObjectsArgs removeObjects = RemoveObjectsArgs.builder()
        .bucket(bucketName)
        .objects(listToDelete)
        .build();

    minioClient.removeObjects(removeObjects);
    System.out.println("Files removed");
  }

  private void initBucketIfNotExists() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    boolean isBucketExists = minioClient.bucketExists(
        BucketExistsArgs.builder().bucket(bucketName).build()
    );
    if (!isBucketExists) {
      minioClient.makeBucket(
          MakeBucketArgs.builder().bucket(bucketName).build()
      );
    }
  }
}
