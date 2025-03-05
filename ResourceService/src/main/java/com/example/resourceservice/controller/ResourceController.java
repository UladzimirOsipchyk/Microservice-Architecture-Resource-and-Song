package com.example.resourceservice.controller;

import com.example.resourceservice.dto.DeletedResourceDTO;
import com.example.resourceservice.dto.ResourceDTO;
import com.example.resourceservice.exception.exceptions.InvalidCsvLengthException;
import com.example.resourceservice.exception.exceptions.InvalidMp3Exception;
import com.example.resourceservice.model.Resource;
import com.example.resourceservice.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/resources")
public class ResourceController {

  @Autowired
  private ResourceService resourceService;

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> getResourceById(@PathVariable Long id) {
    Resource resource = resourceService.getResourceById(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("audio/mpeg"))
        .body(resource.getFileData());
  }

  @PostMapping()
  public ResponseEntity<ResourceDTO> uploadResource(@RequestBody byte[] fileData,
                                                    @RequestHeader("Content-Type") String contentType) throws Exception {

    Resource resource = resourceService.createResource(fileData, contentType);
    return new ResponseEntity<>(new ResourceDTO(resource.getId(), resource.getFileUrl()), HttpStatus.OK);
  }

  @DeleteMapping()
  public ResponseEntity<DeletedResourceDTO> deleteResource(@RequestParam String ids) throws Exception {
    List<Long> deletedIds = resourceService.deleteResource(ids);
    return new ResponseEntity<>(new DeletedResourceDTO(deletedIds), HttpStatus.OK);
  }
}