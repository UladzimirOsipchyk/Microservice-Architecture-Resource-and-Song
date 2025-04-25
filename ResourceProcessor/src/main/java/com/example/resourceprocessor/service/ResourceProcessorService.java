package com.example.resourceprocessor.service;

import com.example.resourceprocessor.dto.SongMetaDataDTO;
import com.example.resourceprocessor.dto.StorageDTO;
import com.example.resourceprocessor.dto.StoragesDTO;
import com.example.resourceprocessor.enums.StorageType;
import com.example.resourceprocessor.service.massaging.RabbitMQProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

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

  @Autowired
  private RabbitMQProducer rabbitMQProducer;

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
  public void executeMetaDataExtraction(Map<String, String> messageData) throws Exception {
    String fileName = messageData.get("key");

    StorageDTO storageDTO = getStorageForMetaExtraction(StorageType.STAGING.getType());

    if (storageDTO != null) {
      downloadFile(fileName, storageDTO);

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

      rabbitMQProducer.processObjectKeyToQueue(
          prepareMessageFromData(songMetaDataDTO.getResourceId().toString(), fileName, "complete_resource")
      );
    }
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
  public StorageDTO getStorageForMetaExtraction(String storageType) throws IOException {
    final StorageDTO[] storageDTO = new StorageDTO[1];
    loadBalancerClient.execute("STORAGESERVICE", storageService -> {
      URI storageUrl = storageService.getUri().resolve("/storages");

      ResponseEntity<StoragesDTO> response = restTemplate.getForEntity(storageUrl, StoragesDTO.class);


      if (response.getStatusCode().is2xxSuccessful()) {
        Objects.requireNonNull(response.getBody()).getStorages()
            .stream()
            .filter(it -> it.getStorageType().equals(storageType))
            .findFirst().ifPresent(storage -> {
              storageDTO[0] = storage;
            });

      }
      return response;
    });

    return storageDTO[0];
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
  private void downloadFile(String downloadPath, StorageDTO storage) {
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

    s3StorageService.downloadFile(downloadPath, storage);
  }

  private String prepareMessageFromData(String ids, String title, String action) throws JsonProcessingException {
    Map<String, String> messageData = new HashMap<>();
    messageData.put("id", ids);
    messageData.put("key", title);
    messageData.put("action", action);
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(messageData);
  }

  @Recover
  public void recover(Exception e) {
    System.err.println("Retries exhausted. Recovering with default");
  }
}