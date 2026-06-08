package com.ouroboros.agenda.adapter.in.web;

import com.ouroboros.agenda.application.EventNotFoundException;
import com.ouroboros.agenda.domain.InvalidEventTimeException;
import com.ouroboros.shared.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz excecoes de dominio/validacao em respostas {@link ApiError}. */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(EventNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      EventNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(InvalidEventTimeException.class)
  public ResponseEntity<ApiError> handleInvalidTime(
      InvalidEventTimeException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
    return build(HttpStatus.BAD_REQUEST, "Requisicao invalida", request, details);
  }

  private static ResponseEntity<ApiError> build(
      HttpStatus status, String message, HttpServletRequest request, List<String> details) {
    ApiError body =
        ApiError.of(
            status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
    return ResponseEntity.status(status).body(body);
  }
}
