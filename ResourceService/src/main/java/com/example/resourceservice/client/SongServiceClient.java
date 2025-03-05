package com.example.resourceservice.client;

import com.example.resourceservice.dto.SongMetaDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "song-service")
public interface SongServiceClient {

  @PostMapping("/songs")
  void saveSongMetadata(SongMetaDataDTO songMetaDataDTO);
}