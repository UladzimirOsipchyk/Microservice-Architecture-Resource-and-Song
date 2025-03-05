package com.example.songservice.exception.exceptions;

public class SongNotFoundException extends RuntimeException {
  public SongNotFoundException(String id) {
    super("The Song Metadata with ID=" + id + " not found");
  }
}