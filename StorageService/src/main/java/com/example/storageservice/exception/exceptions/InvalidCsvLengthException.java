package com.example.storageservice.exception.exceptions;

public class InvalidCsvLengthException extends RuntimeException {
  public InvalidCsvLengthException() {
    super("The CSV length of Storage IDs must be less than 200 characters");
  }
}
