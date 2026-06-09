package com.ouroboros.notes.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Nota pessoal de um usuario, persistida no MongoDB. */
@Document(collection = "notes")
public class Note {

  @Id private String id;

  @Indexed private String userId;

  private String title;
  private String content;
  private List<String> tags;
  private Instant createdAt;
  private Instant updatedAt;

  protected Note() {
    // exigido pelo mapeador do Spring Data
  }

  private Note(
      String id,
      String userId,
      String title,
      String content,
      List<String> tags,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.content = content;
    this.tags = tags;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** Cria uma nova nota pertencente ao usuario informado. */
  public static Note create(String userId, String title, String content, List<String> tags) {
    Instant now = Instant.now();
    return new Note(
        UUID.randomUUID().toString(), userId, title, content, normalizeTags(tags), now, now);
  }

  /** Atualiza os campos editaveis e marca o instante de modificacao. */
  public void update(String title, String content, List<String> tags) {
    this.title = title;
    this.content = content;
    this.tags = normalizeTags(tags);
    this.updatedAt = Instant.now();
  }

  private static List<String> normalizeTags(List<String> tags) {
    return tags == null ? List.of() : List.copyOf(tags);
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public List<String> getTags() {
    return tags;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
