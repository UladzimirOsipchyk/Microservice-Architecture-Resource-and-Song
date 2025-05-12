package com.example.resourceservice.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Resource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String fileName;
  private String storageType;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String fileUrl;
}