package com.ouroboros.finance.adapter.in.web;

import com.ouroboros.finance.adapter.in.web.dto.EntryRequest;
import com.ouroboros.finance.adapter.in.web.dto.EntryResponse;
import com.ouroboros.finance.application.FinanceEntryService;
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
@RequestMapping(ApiPaths.API_V1 + "/finance/entries")
public class FinanceEntryController {

  private final FinanceEntryService entryService;

  public FinanceEntryController(FinanceEntryService entryService) {
    this.entryService = entryService;
  }

  @PostMapping
  public ResponseEntity<EntryResponse> create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EntryRequest request) {
    var entry =
        entryService.create(
            userId(jwt),
            request.categoryId(),
            request.type(),
            request.amount(),
            request.description(),
            request.occurredOn(),
            request.eventId());
    return ResponseEntity.status(HttpStatus.CREATED).body(EntryResponse.from(entry));
  }

  @GetMapping
  public List<EntryResponse> list(@AuthenticationPrincipal Jwt jwt) {
    return entryService.list(userId(jwt)).stream().map(EntryResponse::from).toList();
  }

  @GetMapping("/{id}")
  public EntryResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return EntryResponse.from(entryService.get(userId(jwt), id));
  }

  @PutMapping("/{id}")
  public EntryResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody EntryRequest request) {
    var entry =
        entryService.update(
            userId(jwt),
            id,
            request.categoryId(),
            request.type(),
            request.amount(),
            request.description(),
            request.occurredOn(),
            request.eventId());
    return EntryResponse.from(entry);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    entryService.delete(userId(jwt), id);
    return ResponseEntity.noContent().build();
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
