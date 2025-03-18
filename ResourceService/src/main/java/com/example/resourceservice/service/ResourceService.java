package com.example.resourceservice.service;

import com.example.resourceservice.exception.exceptions.InvalidCsvLengthException;
import com.example.resourceservice.exception.exceptions.InvalidIdException;
import com.example.resourceservice.exception.exceptions.InvalidMp3Exception;
import com.example.resourceservice.exception.exceptions.ResourceNotFoundException;
import com.example.resourceservice.model.Resource;
import com.example.resourceservice.repository.ResourceRepository;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private MetadataExtractorService metadataExtractorService;

  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private S3StorageService s3StorageService;

  public byte[] getResourceBinaryById(Long id) throws IOException {
    if (id == null || id <= 0) {
      throw new InvalidIdException(id == null ? "null" : id.toString());
    }

    Optional<Resource> resource = resourceRepository.findById(id);
    System.out.println("RESOURCE >>> : " + resource);

    if (resource.isPresent()) {
      File file = s3StorageService.downloadFile(resource.get().getFileName());

      System.out.println("File size: " + file.getUsableSpace());
      return Files.readAllBytes(file.toPath());
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

    System.out.println("Meta: " + metadata);
    System.out.println("FileName: " + metadata.get("dc:title"));

    String fileUrl = s3StorageService.uploadFile(metadata.get("dc:title"), fileData, contentType);
    Resource resource = new Resource();
    resource.setFileName(metadata.get("dc:title"));
    resource.setFileUrl(fileUrl);

    System.out.println("UPLOADED FILE URL: " + fileUrl);

    Resource savedResource = resourceRepository.save(resource);

    //todo not needed in this module
//    SongMetaDataDTO songMetaDataDTO = new SongMetaDataDTO(
//        savedResource.getId(),
//        metadata.get("dc:title"),
//        metadata.get("xmpDM:artist"),
//        metadata.get("xmpDM:album"),
//        formatDuration(metadata.get("xmpDM:duration")),
//        metadata.get("xmpDM:releaseDate")
//    );
//
//    loadBalancerClient.execute("SONGSERVICE", songService -> {
//      URI songUri = songService.getUri().resolve("/songs");
//      return restTemplate.postForEntity(songUri, songMetaDataDTO, SongMetaDataDTO.class);
//    });

    return savedResource;
  }

  @Transactional
  public List<Long> deleteResource(String ids) throws Exception {
    System.out.println("deleteResource with Transactional");
    if (ids.length() > 200) {
      throw new InvalidCsvLengthException();
    }

    List<Long> idsList = Arrays.stream(ids.replaceAll(" ", "").split(","))
        .map(Long::parseLong)
        .toList();

    if (!idsList.isEmpty()) {
      //todo not needed in this module
//      loadBalancerClient.execute("SONGSERVICE", songService -> {
//        URI songUri = songService.getUri().resolve("/songs");
//        String urlWithParams = UriComponentsBuilder.fromHttpUrl(songUri.toString())
//            .queryParam("ids", String.join(",", idsList.stream()
//                .map(String::valueOf)
//                .toArray(String[]::new)))
//            .toUriString();
//
//        restTemplate.delete(urlWithParams);
//
//        return "ok";
//      });
//

      s3StorageService.removeFiles(getFileNamesFromResources(idsList));
      resourceRepository.deleteAllById(idsList);
      return idsList;
    }

    return null;
  }

  public static String formatDuration(String seconds) {
    double secondsInDouble = Double.parseDouble(seconds);
    int totalSeconds = (int) Math.round(secondsInDouble);
    int minutes = totalSeconds / 60;
    int remainingSeconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, remainingSeconds);
  }


  private List<String> getFileNamesFromResources(List<Long> ids) {
    System.out.println("getFileNamesFromResources........");
    List<String> fileNames = new ArrayList<>();
    ids.forEach(id -> {
      resourceRepository.findById(id).ifPresent(resource -> {
        fileNames.add(resource.getFileName());
      });
    });
//    List<Resource> resources = resourceRepository.findAllByIdIsIn(ids);
//    return resources.stream()
//        .map(Resource::getFileName)
//        .collect(Collectors.toList());
    return fileNames;
  }
}
