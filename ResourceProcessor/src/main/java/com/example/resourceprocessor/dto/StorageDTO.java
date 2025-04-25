package com.example.resourceprocessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageDTO {

  private String storageType;
  private String bucketName;
  private String path;
}
