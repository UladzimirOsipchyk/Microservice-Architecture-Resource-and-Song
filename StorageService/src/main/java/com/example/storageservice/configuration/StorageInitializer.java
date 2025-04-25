package com.example.storageservice.configuration;

import com.example.storageservice.enums.StorageType;
import com.example.storageservice.model.Storage;
import com.example.storageservice.repository.StorageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageInitializer {

  @Bean
  CommandLineRunner init(StorageRepository storageRepository) {
    return args -> {
      if(storageRepository.count() == 0) {
        storageRepository.save(new Storage(StorageType.STAGING.getType(), StorageType.STAGING.getBucket(), StorageType.STAGING.getPath()));
        storageRepository.save(new Storage(StorageType.PERMANENT.getType(), StorageType.PERMANENT.getBucket(), StorageType.PERMANENT.getPath()));
      }
    };
  }
}
