package com.ouroboros.files.adapter.in.web;

import com.ouroboros.files.application.FileNotFoundException;
import com.ouroboros.shared.ApiError;
import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/** Traduz erros de arquivo em respostas {@link ApiError}; herda o handler de validacao comum. */
@RestControllerAdvice
public class ApiExceptionHandler extends AbstractApiExceptionHandler {

  @ExceptionHandler(FileNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      FileNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ApiError> handleMissingPart(
      MissingServletRequestPartException ex, HttpServletRequest request) {
    return build(
        HttpStatus.BAD_REQUEST,
        "Parte obrigatoria ausente: " + ex.getRequestPartName(),
        request,
        List.of());
  }
}
