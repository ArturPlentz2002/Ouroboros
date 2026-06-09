package com.ouroboros.user.adapter.in.web;

import com.ouroboros.shared.ApiPaths;
import com.ouroboros.user.adapter.in.web.dto.ProfileResponse;
import com.ouroboros.user.adapter.in.web.dto.UpdateProfileRequest;
import com.ouroboros.user.application.UserProfileService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/users")
public class UserProfileController {

  private final UserProfileService service;

  public UserProfileController(UserProfileService service) {
    this.service = service;
  }

  @GetMapping("/me")
  public ProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
    return ProfileResponse.from(service.getOrCreate(userId(jwt), email(jwt)));
  }

  @PutMapping("/me")
  public ProfileResponse updateMe(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
    return ProfileResponse.from(
        service.update(userId(jwt), email(jwt), request.displayName(), request.avatarUrl()));
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }

  private static String email(Jwt jwt) {
    return jwt.getClaimAsString("email");
  }
}
