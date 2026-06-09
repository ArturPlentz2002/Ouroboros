package com.ouroboros.finance.adapter.in.web;

import com.ouroboros.finance.application.CategoryNotFoundException;
import com.ouroboros.finance.application.DuplicateCategoryException;
import com.ouroboros.finance.application.EntryNotFoundException;
import com.ouroboros.shared.ApiError;
import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz excecoes de dominio das financas em respostas {@link ApiError}. */
@RestControllerAdvice
public class ApiExceptionHandler extends AbstractApiExceptionHandler {

  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      CategoryNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(EntryNotFoundException.class)
  public ResponseEntity<ApiError> handleEntryNotFound(
      EntryNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(DuplicateCategoryException.class)
  public ResponseEntity<ApiError> handleDuplicate(
      DuplicateCategoryException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<ApiError> handleBadDate(
      DateTimeParseException ex, HttpServletRequest request) {
    return build(
        HttpStatus.BAD_REQUEST,
        "Parametro de data invalido (use o formato YYYY-MM)",
        request,
        List.of());
  }
}
