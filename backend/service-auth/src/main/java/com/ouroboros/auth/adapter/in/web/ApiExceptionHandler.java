package com.ouroboros.auth.adapter.in.web;

import com.ouroboros.auth.application.EmailAlreadyUsedException;
import com.ouroboros.auth.application.InvalidCredentialsException;
import com.ouroboros.auth.application.InvalidRefreshTokenException;
import com.ouroboros.auth.domain.WeakPasswordException;
import com.ouroboros.shared.ApiError;
import com.ouroboros.shared.web.AbstractApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz excecoes de dominio do auth em respostas {@link ApiError}. */
@RestControllerAdvice
public class ApiExceptionHandler extends AbstractApiExceptionHandler {

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
}
