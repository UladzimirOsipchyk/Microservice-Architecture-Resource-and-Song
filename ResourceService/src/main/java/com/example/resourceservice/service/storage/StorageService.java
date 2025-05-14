package com.example.resourceservice.service.storage;

import com.example.resourceservice.dto.StorageDTO;
import com.example.resourceservice.dto.StoragesDTO;
import com.example.resourceservice.falback.FallbackHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

@Service
public class StorageService {
  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @Autowired
  private RestTemplate restTemplate;


  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
  @CircuitBreaker(name = "storageServiceCircuitBreaker", fallbackMethod = "getStorageFallback")
  public StorageDTO getStorageForType(String storageType) {
    var storageService = loadBalancerClient.choose("STORAGESERVICE");

    if (storageService == null) {
      throw new RuntimeException("No available instances for STORAGESERVICE");
    }

    URI storageUrl = storageService.getUri().resolve("/storages");
    ResponseEntity<StoragesDTO> response = restTemplate.getForEntity(storageUrl, StoragesDTO.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      return Objects.requireNonNull(response.getBody())
          .getStorages()
          .stream()
          .filter(it -> it.getStorageType().equals(storageType))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Storage type not found"));
    }

    throw new RuntimeException("Failed to retrieve storages");
  }

  public static StorageDTO getStorageFallback(String storageType, Throwable t) {
    return FallbackHandler.getStubForStorageStatic(storageType);
  }

}
