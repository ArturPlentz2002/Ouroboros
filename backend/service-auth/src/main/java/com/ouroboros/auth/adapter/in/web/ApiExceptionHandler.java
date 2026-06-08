package com.ouroboros.auth.adapter.in.web;

import com.ouroboros.auth.application.EmailAlreadyUsedException;
import com.ouroboros.auth.application.InvalidCredentialsException;
import com.ouroboros.auth.application.InvalidRefreshTokenException;
import com.ouroboros.auth.domain.WeakPasswordException;
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

  @ExceptionHandler(EmailAlreadyUsedException.class)
  public ResponseEntity<ApiError> handleEmailAlreadyUsed(
      EmailAlreadyUsedException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler(WeakPasswordException.class)
  public ResponseEntity<ApiError> handleWeakPassword(
      WeakPasswordException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
  }

  @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
  public ResponseEntity<ApiError> handleUnauthorized(
      RuntimeException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of());
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
