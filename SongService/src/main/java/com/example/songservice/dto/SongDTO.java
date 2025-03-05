package com.example.songservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongDTO {
  private String name;
  private String artist;
  private String album;
  private String duration;
  private String year;
}
