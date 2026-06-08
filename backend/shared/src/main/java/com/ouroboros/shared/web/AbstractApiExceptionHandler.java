package com.ouroboros.shared.web;

import com.ouroboros.shared.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Base de tratamento de erros reaproveitada pelos servicos: oferece o builder de {@link ApiError} e
 * o handler comum de validacao. Cada servico estende esta classe num {@code @RestControllerAdvice}
 * e adiciona apenas os handlers das suas excecoes de dominio.
 */
public abstract class AbstractApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
    return build(HttpStatus.BAD_REQUEST, "Requisicao invalida", request, details);
  }

  protected ResponseEntity<ApiError> build(
      HttpStatus status, String message, HttpServletRequest request, List<String> details) {
    ApiError body =
        ApiError.of(
            status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
    return ResponseEntity.status(status).body(body);
  }
}
