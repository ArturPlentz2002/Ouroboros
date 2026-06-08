package com.ouroboros.auth.adapter.in.web;

import com.ouroboros.auth.adapter.in.web.dto.LoginRequest;
import com.ouroboros.auth.adapter.in.web.dto.RefreshRequest;
import com.ouroboros.auth.adapter.in.web.dto.RegisterRequest;
import com.ouroboros.auth.adapter.in.web.dto.RegisterResponse;
import com.ouroboros.auth.adapter.in.web.dto.SocialLoginRequest;
import com.ouroboros.auth.adapter.in.web.dto.TokenResponse;
import com.ouroboros.auth.application.AuthService;
import com.ouroboros.auth.application.RegisteredUser;
import com.ouroboros.auth.application.TokenPair;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    RegisteredUser user = authService.register(request.email(), request.password());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new RegisterResponse(user.id(), user.email()));
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenPair tokens = authService.login(request.email(), request.password());
    return ResponseEntity.ok(toResponse(tokens));
  }

  @PostMapping("/social")
  public ResponseEntity<TokenResponse> social(@Valid @RequestBody SocialLoginRequest request) {
    TokenPair tokens = authService.loginWithSocial(request.provider(), request.idToken());
    return ResponseEntity.ok(toResponse(tokens));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    TokenPair tokens = authService.refresh(request.refreshToken());
    return ResponseEntity.ok(toResponse(tokens));
  }

  private static TokenResponse toResponse(TokenPair tokens) {
    return TokenResponse.bearer(
        tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
  }
}
