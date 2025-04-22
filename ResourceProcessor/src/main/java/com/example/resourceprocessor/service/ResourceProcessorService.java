package com.example.resourceprocessor.service;

import com.example.resourceprocessor.dto.SongMetaDataDTO;
import lombok.SneakyThrows;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ResourceProcessorService {
  @Autowired
  private S3StorageService s3StorageService;

  @Autowired
  private MetadataExtractorService metadataExtractorService;

  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @Autowired
  private RestTemplate restTemplate;

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
  public void executeMetaDataExtraction(Map<String, String> messageData) throws Exception {
    String fileName = messageData.get("key");

    downloadFile(fileName);
    Metadata metadata = metadataExtractorService.extractMetadata(fileName);

    SongMetaDataDTO songMetaDataDTO = new SongMetaDataDTO(
        Long.valueOf(messageData.get("id")),
        metadata.get("dc:title"),
        metadata.get("xmpDM:artist"),
        metadata.get("xmpDM:album"),
        MetadataExtractorService.formatDuration(metadata.get("xmpDM:duration")),
        metadata.get("xmpDM:releaseDate")
    );

    loadBalancerClient.execute("SONGSERVICE", songService -> {
      URI songUri = songService.getUri().resolve("/songs");
      return restTemplate.postForEntity(songUri, songMetaDataDTO, SongMetaDataDTO.class);
    });
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
  public void executeMedaDataDeletion(Map<String, String> messageData) throws Exception {
    String ids = messageData.get("id");
    List<Long> idsList = Arrays.stream(ids.replaceAll(" ", "").split(","))
        .map(Long::parseLong)
        .toList();

    loadBalancerClient.execute("SONGSERVICE", songService -> {
      URI songUri = songService.getUri().resolve("/songs");
      String urlWithParams = UriComponentsBuilder.fromHttpUrl(songUri.toString())
          .queryParam("ids", String.join(",", idsList.stream()
              .map(String::valueOf)
              .toArray(String[]::new)))
          .toUriString();

      restTemplate.delete(urlWithParams);

      return "ok";
    });
  }

  @SneakyThrows
  private void downloadFile(String downloadPath) {
    File file = new File(downloadPath);
    if (file.exists()) {
      file.delete();
    }

    File localFile = new File(downloadPath);

    File parentDir = localFile.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new IOException("Failed to create parent directories for " + localFile.getAbsolutePath());
      }
    }

    s3StorageService.downloadFile(downloadPath);
  }

  @Recover
  public void recover(Exception e) {
    System.err.println("Retries exhausted. Recovering with default");
  }
}