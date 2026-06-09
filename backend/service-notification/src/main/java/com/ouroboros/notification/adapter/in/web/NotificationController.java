package com.ouroboros.notification.adapter.in.web;

import com.ouroboros.notification.adapter.in.web.dto.NotificationResponse;
import com.ouroboros.notification.application.NotificationService;
import com.ouroboros.shared.ApiPaths;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/notifications")
public class NotificationController {

  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping
  public List<NotificationResponse> list(@AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return service.list(userId).stream().map(NotificationResponse::from).toList();
  }
}
