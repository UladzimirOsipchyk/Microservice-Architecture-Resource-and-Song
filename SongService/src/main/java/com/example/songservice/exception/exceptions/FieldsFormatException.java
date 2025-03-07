package com.example.songservice.exception.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldsFormatException extends RuntimeException {
  private Map<String, String> details;
}
