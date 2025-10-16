package com.TelcoNova_2025_2.TelcoNovaP7_Backend.common;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> handleApi(ApiException ex) {
    var body = java.util.Map.of(
      "status", ex.getStatus().value(),
      "error",  ex.getStatus().getReasonPhrase(),
      "message", ex.getMessage(),
      "timestamp", java.time.OffsetDateTime.now().toString()
    );
    return ResponseEntity.status(ex.getStatus()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    var errors = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> java.util.Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
      .toList();

    var body = java.util.Map.of(
      "status", 400,
      "error",  "Bad Request",
      "message","Validación fallida",
      "errors", errors,
      "timestamp", java.time.OffsetDateTime.now().toString()
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleUnreadable(HttpMessageNotReadableException ex) {
    var body = java.util.Map.of(
      "status", 400,
      "error",  "Bad Request",
      "message","Cuerpo JSON inválido o tipos de dato incorrectos",
      "timestamp", java.time.OffsetDateTime.now().toString()
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleOther(Exception ex) {
    var body = java.util.Map.of(
      "status", 500,
      "error",  "Internal Server Error",
      "message","Ocurrió un error inesperado",
      "timestamp", java.time.OffsetDateTime.now().toString()
    );
    return ResponseEntity.status(500).body(body);
  }
}
