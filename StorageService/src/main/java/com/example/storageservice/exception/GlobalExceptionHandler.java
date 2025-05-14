package com.example.storageservice.exception;

import com.example.storageservice.exception.exceptions.FieldsFormatException;
import com.example.storageservice.exception.exceptions.InvalidCsvLengthException;
import com.example.storageservice.exception.exceptions.StorageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(StorageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(StorageNotFoundException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            ex.getMessage(),
            null,
            HttpStatus.NOT_FOUND.value()
        ),
        HttpStatus.NOT_FOUND
    );
  }

  @ExceptionHandler(InvalidCsvLengthException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCsvLength(InvalidCsvLengthException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            ex.getMessage(),
            null,
            HttpStatus.BAD_REQUEST.value()
        ),
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(FieldsFormatException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCsvLength(FieldsFormatException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(
            "Validation error",
            ex.getDetails(),
            HttpStatus.BAD_REQUEST.value()
        ),
        HttpStatus.BAD_REQUEST
    );
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalServerError(Exception ex) {
    if (ex instanceof NumberFormatException) {
      String message = "Invalid value '" + ex.getMessage() + "' for ID. Only positive integers are allowed.";
      return new ResponseEntity<>(
          new ErrorResponse(message, null, HttpStatus.BAD_REQUEST.value()),
          HttpStatus.BAD_REQUEST
      );
    } else if(ex instanceof AuthorizationDeniedException) {
      return new ResponseEntity<>(
          new ErrorResponse(ex.getMessage(), null, HttpStatus.UNAUTHORIZED.value()),
          HttpStatus.UNAUTHORIZED
      );
    }
    ex.printStackTrace();
    return new ResponseEntity<>(
        new ErrorResponse(
            "An internal server error has occurred.",
            null,
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        ),
        HttpStatus.INTERNAL_SERVER_ERROR
    );
  }
}