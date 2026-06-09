package com.ouroboros.agenda.adapter.in.web;

import com.ouroboros.agenda.application.EventNotFoundException;
import com.ouroboros.agenda.domain.InvalidEventTimeException;
import com.ouroboros.shared.ApiError;
import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz excecoes de dominio da agenda em respostas {@link ApiError}. */
@RestControllerAdvice
public class ApiExceptionHandler extends AbstractApiExceptionHandler {

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
}
