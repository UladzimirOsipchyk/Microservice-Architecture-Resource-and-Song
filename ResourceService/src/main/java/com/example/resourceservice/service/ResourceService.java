package com.example.resourceservice.service;

import com.example.resourceservice.exception.exceptions.InvalidCsvLengthException;
import com.example.resourceservice.exception.exceptions.InvalidIdException;
import com.example.resourceservice.exception.exceptions.InvalidMp3Exception;
import com.example.resourceservice.exception.exceptions.ResourceNotFoundException;
import com.example.resourceservice.model.Resource;
import com.example.resourceservice.repository.ResourceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

  public byte[] getResourceBinaryById(Long id) throws IOException {
    if (id == null || id <= 0) {
      throw new InvalidIdException(id == null ? "null" : id.toString());
    }

    Optional<Resource> resource = resourceRepository.findById(id);

    if (resource.isPresent()) {
      File file = s3StorageService.downloadFile(resource.get().getFileName());
      File tmpFile = File.createTempFile("song", "mp3", file);
      file.delete();
      return Files.readAllBytes(tmpFile.toPath());
    } else {

      throw new ResourceNotFoundException(String.valueOf(id));
    }
  }

  @Transactional
  public Resource createResource(byte[] fileData, String contentType) throws Exception {
    if (!contentType.equalsIgnoreCase("audio/mpeg")) {
      throw new InvalidMp3Exception(contentType);
    }

    Metadata metadata = metadataExtractorService.extractMetadata(fileData);
    String fileName = metadata.get("dc:title");
    String fileUrl = s3StorageService.uploadFile(fileName, fileData, contentType);

    Resource resource = new Resource();
    resource.setFileName(fileName);
    resource.setFileUrl(fileUrl);

    Resource savedResource = resourceRepository.save(resource);

    rabbitMQProducer.processObjectKeyToQueue(
        prepareMessageFromData(resource.getId().toString(), fileName, "extract_meta")
    );

    return savedResource;
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
      s3StorageService.removeFiles(getFileNamesFromResources(idsList));

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
