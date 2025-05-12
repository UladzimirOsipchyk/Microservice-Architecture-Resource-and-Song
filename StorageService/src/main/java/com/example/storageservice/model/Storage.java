package com.example.storageservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Storage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String storageType;
  private String bucketName;
  private String path;
  public Storage() {
  }

  public Storage(String storageType, String bucketName, String path) {
    this.storageType = storageType;
    this.bucketName = bucketName;
    this.path = path;
  }
}
