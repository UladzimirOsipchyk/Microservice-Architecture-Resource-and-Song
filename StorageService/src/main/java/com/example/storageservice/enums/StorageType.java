package com.example.storageservice.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum StorageType {
  STAGING("STAGING", "staging-songs", "/files"),
  PERMANENT("PERMANENT", "permanent-songs", "/files");

  StorageType(String type, String bucket, String path) {
    this.type = type;
    this.bucket = bucket;
    this.path = path;
  }

  private final String type;
  private final String bucket;
  private final String path;

  }
