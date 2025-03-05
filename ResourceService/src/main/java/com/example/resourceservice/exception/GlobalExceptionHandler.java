package com.example.resourceservice.exception;

import com.example.resourceservice.exception.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidMp3Exception.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(InvalidMp3Exception ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
            HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(InvalidIdException.class)
  public ResponseEntity<ErrorResponse> handleInvalidID(InvalidIdException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            ex.getMessage(), HttpStatus.NOT_FOUND.value()),
        HttpStatus.NOT_FOUND
    );
  }

  @ExceptionHandler(InvalidCsvLengthException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCsvLength(InvalidCsvLengthException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalServerError(Exception ex) {
    System.out.println("EXCEPTION !!! : " + ex);
    if (ex instanceof MethodArgumentTypeMismatchException) {
      String message = "Invalid value '" + ((MethodArgumentTypeMismatchException) ex).getValue() + "' for ID. Must be a positive integer.";
      return new ResponseEntity<>(
          new ErrorResponse(message, HttpStatus.BAD_REQUEST.value()),
          HttpStatus.BAD_REQUEST
      );
    } else if (ex instanceof NumberFormatException) {
      String message = "Invalid value '" + ex.getMessage() + "' for ID. Only positive integers are allowed.";
      return new ResponseEntity<>(
          new ErrorResponse(message, HttpStatus.BAD_REQUEST.value()),
          HttpStatus.BAD_REQUEST
      );
    }
    return new ResponseEntity<>(
        new ErrorResponse(
            "An internal server error has occurred.",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        ),
        HttpStatus.INTERNAL_SERVER_ERROR
    );
  }
}