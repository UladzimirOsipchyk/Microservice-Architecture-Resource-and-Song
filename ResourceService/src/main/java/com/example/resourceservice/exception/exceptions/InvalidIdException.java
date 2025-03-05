package com.example.resourceservice.exception.exceptions;

public class InvalidIdException extends RuntimeException {
  public InvalidIdException(String input) {
    super("Invalid value for '" + input + "' Must be a positive integer.");
  }
}
