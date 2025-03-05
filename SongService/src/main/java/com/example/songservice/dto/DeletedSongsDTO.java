package com.example.songservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeletedSongsDTO {
  private final List<Long> ids;
}
