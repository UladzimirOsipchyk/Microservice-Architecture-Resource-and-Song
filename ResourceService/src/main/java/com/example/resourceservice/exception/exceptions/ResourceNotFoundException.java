package com.example.resourceservice.exception.exceptions;
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String id) {
    super("The Resource with ID=" + id + " not found");
  }
}