package com.example.resourceservice.service;

import com.example.resourceservice.client.SongServiceClient;
import com.example.resourceservice.dto.SongMetaDataDTO;
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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

  public Resource getResourceById(Long id) {
    if (id == null || id <= 0) {
      throw new InvalidIdException(id == null ? "null" : id.toString());
    }

    Optional<Resource> resource = resourceRepository.findById(id);

    if (resource.isPresent()) {
      return resource.get();
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
    resource.setFileUrl(fileUrl);

    System.out.println("UPLOADED FILE URL: " + fileUrl);

    Resource savedResource = resourceRepository.save(resource);

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

  public List<Long> deleteResource(String ids) throws Exception {
    if (ids.length() > 200) {
      throw new InvalidCsvLengthException();
    }

    List<Long> idsList = Arrays.stream(ids.replaceAll(" ", "").split(","))
        .map(Long::parseLong)
        .toList();

    if (!idsList.isEmpty()) {
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
}
