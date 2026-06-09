package com.ouroboros.finance.adapter.in.web;

import com.ouroboros.finance.application.CategoryNotFoundException;
import com.ouroboros.finance.application.DuplicateCategoryException;
import com.ouroboros.finance.application.EntryNotFoundException;
import com.ouroboros.shared.ApiError;
import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
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

  /**
   * Rede de seguranca para corridas: se duas requisicoes concorrentes passarem pela checagem de
   * unicidade e a segunda violar o indice unico do banco, devolve 409 em vez de 500.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "Conflito de dados (registro duplicado)", request, List.of());
  }
}
