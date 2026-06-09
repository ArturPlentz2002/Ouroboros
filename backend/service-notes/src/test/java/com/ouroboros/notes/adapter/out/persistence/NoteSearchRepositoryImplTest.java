package com.ouroboros.notes.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

class NoteSearchRepositoryImplTest {

  private static final String USER = "user-1";

  @Test
  void semFiltrosEscopaSoPorUsuarioEOrdenaPorUpdatedAt() {
    Query q = NoteSearchRepositoryImpl.buildQuery(USER, null, null);

    assertThat(q.getQueryObject().toString()).contains("userId", USER);
    assertThat(q.getQueryObject().toString()).doesNotContain("tags", "title");
    assertThat(q.getSortObject().toString()).contains("updatedAt", "-1");
  }

  @Test
  void filtraPorTag() {
    Query q = NoteSearchRepositoryImpl.buildQuery(USER, "work", null);

    assertThat(q.getQueryObject().toString()).contains("tags", "work");
  }

  @Test
  void filtraPorTextoComRegexCaseInsensitiveEmTituloEConteudo() {
    Query q = NoteSearchRepositoryImpl.buildQuery(USER, null, "leite");

    String doc = q.getQueryObject().toString();
    assertThat(doc).contains("title", "content", "leite");
    // opcoes do regex case-insensitive
    assertThat(doc).contains("i");
  }
}
