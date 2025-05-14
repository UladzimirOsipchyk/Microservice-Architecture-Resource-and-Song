package com.example.resourceservice.controller;

import com.example.resourceservice.dto.DeletedResourceDTO;
import com.example.resourceservice.dto.ResourceDTO;
import com.example.resourceservice.dto.StorageDTO;
import com.example.resourceservice.falback.FallbackHandler;
import com.example.resourceservice.model.Resource;
import com.example.resourceservice.service.resource.ResourceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {

  @Autowired
  private ResourceService resourceService;

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> getResourceById(@PathVariable Long id) throws Exception{
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("audio/mpeg"))
        .body(resourceService.getResourceBinaryById(id));
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