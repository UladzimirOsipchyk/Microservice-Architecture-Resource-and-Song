package com.example.storageservice.exception.exceptions;

public class StorageNotFoundException extends RuntimeException {
  public StorageNotFoundException(String id) {
    super("The Storage with ID=" + id + " not found");
  }
}