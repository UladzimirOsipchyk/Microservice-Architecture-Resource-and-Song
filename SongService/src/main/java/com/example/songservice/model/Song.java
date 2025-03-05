package com.example.songservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Song {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long resourceId;
  private String name;
  private String artist;
  private String album;
  private String length;
  private String year;
}