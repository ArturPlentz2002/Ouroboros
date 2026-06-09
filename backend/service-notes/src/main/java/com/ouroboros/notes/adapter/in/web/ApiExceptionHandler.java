package com.ouroboros.notes.adapter.in.web;

import com.ouroboros.notes.application.NoteNotFoundException;
import com.ouroboros.shared.ApiError;
import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz excecoes de dominio das notas em respostas {@link ApiError}. */
@RestControllerAdvice
public class ApiExceptionHandler extends AbstractApiExceptionHandler {

  @ExceptionHandler(NoteNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      NoteNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
  }
}
