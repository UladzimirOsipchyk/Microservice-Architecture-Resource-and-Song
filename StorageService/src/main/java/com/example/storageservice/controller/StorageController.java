package com.example.storageservice.controller;

import com.example.storageservice.dto.*;
import com.example.storageservice.model.Storage;
import com.example.storageservice.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/storages")
public class StorageController {
  @Autowired
  private StorageService storageService;

  @GetMapping
  public ResponseEntity<StorageListDto> getAllStorages() throws Exception {
    List<Storage> storages = storageService.getAllStorages();
    List<StorageDto> storageDtoList = new ArrayList<>();
    storages.forEach(it -> {
      storageDtoList.add(new StorageDto(it.getId(), it.getStorageType(), it.getBucketName(), it.getPath()));
    });
    return new ResponseEntity<>(new StorageListDto(storageDtoList), HttpStatus.OK);
  }
  @GetMapping("/{id}")
  public ResponseEntity<StorageDto> getStorageById(@PathVariable Long id) throws Exception {

    Storage storage = storageService.getStorageById(id);
    StorageDto storageDto = new StorageDto(
        storage.getId(),
        storage.getStorageType(),
        storage.getBucketName(),
        storage.getPath()
    );

    return new ResponseEntity<>(storageDto, HttpStatus.OK);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('SCOPE_write')")
  public ResponseEntity<CreatedStorageDto> createSTorage(@RequestBody StorageRequestDto storageRequestDTO) throws Exception {
    Storage storage = storageService.createStorage(storageRequestDTO);
    return new ResponseEntity<>(new CreatedStorageDto(storage.getId()), HttpStatus.OK);
  }

  @DeleteMapping()
  @PreAuthorize("hasAuthority('SCOPE_write')")
  public ResponseEntity<DeletedStorageDto> deleteResource(@RequestParam String ids) throws Exception{
    List<Long> deletedIds = storageService.deleteStorages(ids);
    return new ResponseEntity<>(new DeletedStorageDto(deletedIds), HttpStatus.OK);
  }
}
