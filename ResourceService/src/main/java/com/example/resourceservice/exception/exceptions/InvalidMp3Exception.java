package com.example.resourceservice.exception.exceptions;

public class InvalidMp3Exception extends RuntimeException{
  public InvalidMp3Exception(String contentType) {
    super("Validation failed " + contentType + " - invalid MP3");
  }
}
