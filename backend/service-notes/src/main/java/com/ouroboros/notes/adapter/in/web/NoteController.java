package com.ouroboros.notes.adapter.in.web;

import com.ouroboros.notes.adapter.in.web.dto.NoteRequest;
import com.ouroboros.notes.adapter.in.web.dto.NoteResponse;
import com.ouroboros.notes.application.NoteService;
import com.ouroboros.shared.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/notes")
public class NoteController {

  private final NoteService noteService;

  public NoteController(NoteService noteService) {
    this.noteService = noteService;
  }

  @PostMapping
  public ResponseEntity<NoteResponse> create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NoteRequest request) {
    var note = noteService.create(userId(jwt), request.title(), request.content(), request.tags());
    return ResponseEntity.status(HttpStatus.CREATED).body(NoteResponse.from(note));
  }

  @GetMapping
  public List<NoteResponse> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) String tag,
      @RequestParam(name = "q", required = false) String q) {
    return noteService.search(userId(jwt), tag, q).stream().map(NoteResponse::from).toList();
  }

  @GetMapping("/{id}")
  public NoteResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
    return NoteResponse.from(noteService.get(userId(jwt), id));
  }

  @PutMapping("/{id}")
  public NoteResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String id,
      @Valid @RequestBody NoteRequest request) {
    var note =
        noteService.update(userId(jwt), id, request.title(), request.content(), request.tags());
    return NoteResponse.from(note);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
    noteService.delete(userId(jwt), id);
    return ResponseEntity.noContent().build();
  }

  private static String userId(Jwt jwt) {
    return jwt.getSubject();
  }
}
