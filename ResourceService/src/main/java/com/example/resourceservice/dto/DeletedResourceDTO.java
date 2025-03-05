package com.example.resourceservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeletedResourceDTO {
  private final List<Long> ids;
}
