package com.example.resourceservice.service.storage;

import com.example.resourceservice.dto.StorageDTO;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class S3StorageService {
  private final MinioClient minioClient;

  public S3StorageService(@Value("${cloud.aws.s3.endpoint}") String endpoint,
                          @Value("${cloud.aws.s3.access-key}") String accessKey,
                          @Value("${cloud.aws.s3.secret-key}") String secretKey) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    this.minioClient = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }

  @SneakyThrows
  public String uploadFileToStorage(String fileName, byte[] fileData, String contentType, StorageDTO storage) {
    initBucketIfNotExists(storage.getBucketName());

    InputStream inputStream = new ByteArrayInputStream(fileData);
    PutObjectArgs objectArgs = PutObjectArgs.builder()
        .bucket(storage.getBucketName())
        .object(fileName)
        .stream(inputStream, fileData.length, -1)
        .contentType(contentType)
        .build();

    minioClient.putObject(objectArgs);

    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(storage.getBucketName())
            .object(fileName)
            .expiry(1, TimeUnit.DAYS)
            .build()
    );
  }

  @SneakyThrows
  public File downloadFileFromStorage(String fileName, StorageDTO storage) {

    DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
        .bucket(storage.getBucketName())
        .object(fileName)
        .filename(fileName)
        .build();

    minioClient.downloadObject(downloadObjectArgs);

    return new File(downloadObjectArgs.filename());
  }

  @SneakyThrows
  public void removeFile(String fileName, StorageDTO storage) {

    RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
        .bucket(storage.getBucketName())
        .object(fileName)
        .build();

    minioClient.removeObject(removeObjectArgs);
  }

  @SneakyThrows
  @Transactional
  public void removeFiles(List<String> fileNamesList, StorageDTO storage) {
    List<DeleteObject> listToDelete = new LinkedList<>();
    fileNamesList.forEach(it -> listToDelete.add(new DeleteObject(it)));

    RemoveObjectsArgs removeObjects = RemoveObjectsArgs.builder()
        .bucket(storage.getBucketName())
        .objects(listToDelete)
        .build();

    minioClient.removeObjects(removeObjects);
  }

  @SneakyThrows
  @Transactional
  public void copyFile(String fileName, StorageDTO storageFrom, StorageDTO storageTo) {
    initBucketIfNotExists(storageTo.getBucketName());

    minioClient.copyObject(
        CopyObjectArgs.builder()
            .bucket(storageTo.getBucketName())
            .object(fileName)
            .source(
                CopySource.builder()
                    .bucket(storageFrom.getBucketName())
                    .object(fileName)
                    .build()
            )
            .build()
    );

  }

  private void initBucketIfNotExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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
