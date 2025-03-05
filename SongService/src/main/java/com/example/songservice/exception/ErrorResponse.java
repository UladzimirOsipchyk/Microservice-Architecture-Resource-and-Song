package com.example.songservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ErrorResponse {
  private String errorMessage;
  private Map<String, String> details;
  private Integer errorCode;
}
