package com.example.songservice.exception.exceptions;

public class InvalidCsvLengthException extends RuntimeException {
  public InvalidCsvLengthException() {
    super("The CSV length of Songs IDs must be less than 200 characters");
  }
}
