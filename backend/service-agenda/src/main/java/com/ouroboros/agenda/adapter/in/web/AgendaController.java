package com.ouroboros.agenda.adapter.in.web;

import com.ouroboros.agenda.adapter.in.web.dto.EventRequest;
import com.ouroboros.agenda.adapter.in.web.dto.EventResponse;
import com.ouroboros.agenda.application.AgendaService;
import com.ouroboros.shared.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/agenda/events")
public class AgendaController {

  private final AgendaService agendaService;

  public AgendaController(AgendaService agendaService) {
    this.agendaService = agendaService;
  }

  @PostMapping
  public ResponseEntity<EventResponse> create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EventRequest request) {
    var event =
        agendaService.create(
            userId(jwt),
            request.title(),
            request.description(),
            request.startsAt(),
            request.endsAt());
    return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
  }

  @GetMapping
  public List<EventResponse> list(@AuthenticationPrincipal Jwt jwt) {
    return agendaService.list(userId(jwt)).stream().map(EventResponse::from).toList();
  }

  @GetMapping("/{id}")
  public EventResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return EventResponse.from(agendaService.get(userId(jwt), id));
  }

  @PutMapping("/{id}")
  public EventResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody EventRequest request) {
    var event =
        agendaService.update(
            userId(jwt),
            id,
            request.title(),
            request.description(),
            request.startsAt(),
            request.endsAt());
    return EventResponse.from(event);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    agendaService.delete(userId(jwt), id);
    return ResponseEntity.noContent().build();
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
