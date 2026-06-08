package com.ouroboros.auth.adapter.in.web;

import com.ouroboros.auth.adapter.in.web.dto.RegisterRequest;
import com.ouroboros.auth.adapter.in.web.dto.RegisterResponse;
import com.ouroboros.auth.application.AuthService;
import com.ouroboros.auth.application.RegisteredUser;
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
}
