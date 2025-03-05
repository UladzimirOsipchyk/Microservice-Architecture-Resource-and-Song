package com.example.resourceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceDTO {
  private final Long id;
  private String fileUrl;
}
