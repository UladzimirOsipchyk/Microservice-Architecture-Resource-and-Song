package com.example.resourceprocessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongMetaDataDTO {
  private Long resourceId;
  private String name;
  private String artist;
  private String album;
  private String duration;
  private String year;
}




