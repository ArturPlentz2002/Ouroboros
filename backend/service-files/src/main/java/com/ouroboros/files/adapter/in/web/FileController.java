package com.ouroboros.files.adapter.in.web;

import com.ouroboros.files.adapter.in.web.dto.FileResponse;
import com.ouroboros.files.application.FileDownload;
import com.ouroboros.files.application.FileService;
import com.ouroboros.files.domain.FileMetadata;
import com.ouroboros.shared.ApiPaths;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/files")
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @PostMapping
  public ResponseEntity<FileResponse> upload(
      @AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file)
      throws IOException {
    var meta =
        fileService.store(
            userId(jwt), file.getOriginalFilename(), file.getContentType(), file.getBytes());
    return ResponseEntity.status(HttpStatus.CREATED).body(FileResponse.from(meta));
  }

  @GetMapping
  public List<FileResponse> list(@AuthenticationPrincipal Jwt jwt) {
    return fileService.list(userId(jwt)).stream().map(FileResponse::from).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    FileDownload file = fileService.download(userId(jwt), id);
    FileMetadata meta = file.metadata();
    MediaType contentType =
        meta.getContentType() == null
            ? MediaType.APPLICATION_OCTET_STREAM
            : MediaType.parseMediaType(meta.getContentType());
    return ResponseEntity.ok()
        .contentType(contentType)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(meta.getFilename()).build().toString())
        .body(file.content());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    fileService.delete(userId(jwt), id);
    return ResponseEntity.noContent().build();
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
