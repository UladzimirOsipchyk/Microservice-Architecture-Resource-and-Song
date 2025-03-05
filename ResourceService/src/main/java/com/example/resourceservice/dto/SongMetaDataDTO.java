package com.example.resourceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongMetaDataDTO {
  private Long resourceId;
  private String name;
  private String artist;
  private String album;
  private String duration;
  private String year;
}
