package com.example.storageservice.service;

import com.example.storageservice.dto.StorageRequestDto;
import com.example.storageservice.enums.StorageType;
import com.example.storageservice.exception.exceptions.FieldsFormatException;
import com.example.storageservice.exception.exceptions.InvalidCsvLengthException;
import com.example.storageservice.exception.exceptions.StorageNotFoundException;
import com.example.storageservice.model.Storage;
import com.example.storageservice.repository.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StorageService {
  @Autowired
  private StorageRepository storageRepository;

  public List<Storage> getAllStorages() {
    return storageRepository.findAll();
  }

  public Storage getStorageById(Long id) {
    Optional<Storage> storage = storageRepository.findById(id);

    if (storage.isPresent()) {
      return storage.get();
    } else {
      throw new StorageNotFoundException(String.valueOf(id));
    }
  }

  public Storage createStorage(StorageRequestDto storageRequestDto) throws Exception {
    validateStorage(storageRequestDto);

    Storage storage = new Storage();
    storage.setStorageType(storageRequestDto.getStorageType());
    storage.setBucketName(storageRequestDto.getBucketName());
    storage.setPath(storage.getPath());
    return storageRepository.save(storage);
  }

  public List<Long> deleteStorages(String ids) throws Exception {
    if (ids.length() > 200) {
      throw new InvalidCsvLengthException();
    }

    List<Long> idsList = Arrays.stream(ids.replaceAll(" ", "").split(","))
        .map(Long::parseLong)
        .toList();

    List<Storage> storages = storageRepository.findAllByIdIsIn(idsList);
    if (!storages.isEmpty()) {
      storageRepository.deleteAll(storages);
      return idsList;
    }

    return null;
  }

  private void validateStorage(StorageRequestDto storageRequestDto) {
    Map<String, String> validationErrors = new HashMap<>();
    String storageType = storageRequestDto.getStorageType();
    if (storageType != null) {
      if(!storageType.equals(StorageType.STAGING.getType()) || !storageType.equals(StorageType.PERMANENT.getType())) {
        validationErrors.put(storageRequestDto.getStorageType(), "invalid storage type");
      }
    } else {
      validationErrors.put("StorageType", "cannot be NULL");
    }

    if (!validationErrors.isEmpty()) {
      throw new FieldsFormatException(validationErrors);
    }
  }

}
