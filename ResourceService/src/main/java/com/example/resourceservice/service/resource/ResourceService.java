package com.example.resourceservice.service.resource;

import com.example.resourceservice.dto.StorageDTO;
import com.example.resourceservice.dto.StoragesDTO;
import com.example.resourceservice.enums.StorageType;
import com.example.resourceservice.exception.exceptions.InvalidCsvLengthException;
import com.example.resourceservice.exception.exceptions.InvalidIdException;
import com.example.resourceservice.exception.exceptions.InvalidMp3Exception;
import com.example.resourceservice.exception.exceptions.ResourceNotFoundException;
import com.example.resourceservice.falback.FallbackHandler;
import com.example.resourceservice.model.Resource;
import com.example.resourceservice.repository.ResourceRepository;
import com.example.resourceservice.service.metadata.MetadataExtractorService;
import com.example.resourceservice.service.storage.S3StorageService;
import com.example.resourceservice.service.messaging.RabbitMQProducer;
import com.example.resourceservice.service.storage.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ResourceService {

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private MetadataExtractorService metadataExtractorService;

  @Autowired
  private S3StorageService s3StorageService;

  @Autowired
  private RabbitMQProducer rabbitMQProducer;

  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private StorageService storageService;



  public byte[] getResourceBinaryById(Long id) throws Exception {
    if (id == null || id <= 0) {
      throw new InvalidIdException(id == null ? "null" : id.toString());
    }

    Optional<Resource> resource = resourceRepository.findById(id);

    if (resource.isPresent()) {
      Path destination = Paths.get(resource.get().getFileName());
      if (Files.exists(destination)) {
        Files.delete(destination);
      }
      File file = s3StorageService.downloadFileFromStorage(
          resource.get().getFileName(),
          storageService.getStorageForType(StorageType.PERMANENT.getType())
      );

      return Files.readAllBytes(file.toPath());
    } else {

      throw new ResourceNotFoundException(String.valueOf(id));
    }
  }

  @Transactional
  public Resource createResource(byte[] fileData, String contentType) throws Exception {
    if (!contentType.contains("audio/mpeg")) {
      throw new InvalidMp3Exception(contentType);
    }

    Metadata metadata = metadataExtractorService.extractMetadata(fileData);
    String fileName = metadata.get("dc:title");

    StorageDTO storage = storageService.getStorageForType(StorageType.STAGING.getType());
    String fileUrl = s3StorageService.uploadFileToStorage(fileName, fileData, contentType, storage);

    Resource resource = new Resource();
    resource.setFileName(fileName);
    resource.setFileUrl(fileUrl);
    resource.setStorageType(storage.getStorageType());

    Resource savedResource = resourceRepository.save(resource);

    rabbitMQProducer.processObjectKeyToQueue(
        prepareMessageFromData(resource.getId().toString(), fileName, "extract_meta")
    );

    return savedResource;
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
  public void processCompleteResourceProcessing(Map<String, String> messageData) throws Exception {
    String fileName = messageData.get("key");
    Long resourceId = Long.valueOf(messageData.get("id"));
    s3StorageService.copyFile(
        fileName,
        storageService.getStorageForType(StorageType.STAGING.getType()),
        storageService.getStorageForType(StorageType.PERMANENT.getType())
    );

    resourceRepository.findById(resourceId).ifPresent(resource -> {
      resource.setStorageType(StorageType.PERMANENT.getType());
      resourceRepository.save(resource);
    });
  }

    @Transactional
  public List<Long> deleteResource(String ids) throws Exception {
    if (ids.length() > 200) {
      throw new InvalidCsvLengthException();
    }

    List<Long> idsList = Arrays.stream(ids.replaceAll(" ", "").split(","))
        .map(Long::parseLong)
        .toList();

    if (!idsList.isEmpty()) {
      s3StorageService.removeFiles(
          getFileNamesFromResources(idsList),
          storageService.getStorageForType(StorageType.PERMANENT.getType())
      );

      rabbitMQProducer.processObjectKeyToQueue(
          prepareMessageFromData(ids, null, "remove_meta")
      );

      resourceRepository.deleteAllById(idsList);
      return idsList;
    }

    return null;
  }

  private List<String> getFileNamesFromResources(List<Long> ids) {
    List<String> fileNames = new ArrayList<>();
    ids.forEach(id -> {
      resourceRepository.findById(id).ifPresent(resource -> {
        fileNames.add(resource.getFileName());
      });
    });
    return fileNames;
  }

  private String prepareMessageFromData(String ids, String title, String action) throws JsonProcessingException {
    Map<String, String> messageData = new HashMap<>();
    messageData.put("id", ids);
    messageData.put("key", title);
    messageData.put("action", action);
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(messageData);
  }
}
