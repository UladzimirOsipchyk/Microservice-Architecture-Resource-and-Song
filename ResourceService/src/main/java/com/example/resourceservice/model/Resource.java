package com.example.resourceservice.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Resource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Lob
  private byte[] fileData;
}