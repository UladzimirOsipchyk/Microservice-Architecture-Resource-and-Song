package com.example.resourceservice.falback;

import com.example.resourceservice.dto.StorageDTO;
import com.example.resourceservice.dto.StoragesDTO;
import com.example.resourceservice.enums.StorageType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FallbackHandler {

  public static StorageDTO getStubForStorageStatic(String storageType) {
    List<StorageDTO> storages = List.of(
        new StorageDTO(StorageType.STAGING.getType(), "staging-song", "/files"),
        new StorageDTO(StorageType.PERMANENT.getType(), "permanent-song", "/files")
    );

    return storages.stream()
        .filter(it -> it.getStorageType().equals(storageType))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Stub not available for storage type: " + storageType));
  }
}
