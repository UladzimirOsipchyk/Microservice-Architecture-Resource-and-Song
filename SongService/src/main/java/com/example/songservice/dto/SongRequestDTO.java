package com.example.songservice.dto;

import lombok.Data;

@Data
public class SongRequestDTO {
  private Long resourceId;
  private String name;
  private String artist;
  private String album;
  private String duration;
  private String year;
}
