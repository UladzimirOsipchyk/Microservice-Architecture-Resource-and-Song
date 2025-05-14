package com.example.storageservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeletedStorageDto {
  private final List<Long> ids;
}
