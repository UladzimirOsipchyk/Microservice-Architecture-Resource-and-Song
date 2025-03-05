package com.example.resourceservice.exception.exceptions;

public class InvalidCsvLengthException extends RuntimeException {
  public InvalidCsvLengthException() {
    super("The CSV length of resource IDs must be less than 200 characters");
  }
}
