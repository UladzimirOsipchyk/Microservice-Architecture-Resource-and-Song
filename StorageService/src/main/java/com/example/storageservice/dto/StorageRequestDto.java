package com.example.storageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StorageRequestDto {
  private String storageType;
  private String bucketName;
  private String path;

}
