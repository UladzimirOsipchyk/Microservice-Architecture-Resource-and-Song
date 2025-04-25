package com.example.storageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StorageDto {
  private Long id;
  private String storageType;
  private String bucketName;
  private String path;
}
