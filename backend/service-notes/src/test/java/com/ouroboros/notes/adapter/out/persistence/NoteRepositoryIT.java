package com.ouroboros.notes.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.ouroboros.notes.domain.Note;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * IT de persistencia: exercita o {@link NoteRepository} (derivadas + fragmento de busca) contra um
 * MongoDB real, complementando o IT de API ({@code NoteIT}). Foca no que o slice de persistencia
 * garante: ordenacao por modificacao, escopo por usuario e os filtros do {@code search}.
 */
@DataMongoTest
@Testcontainers
class NoteRepositoryIT {

  private static final String USER = "11111111-1111-1111-1111-111111111111";
  private static final String OTHER = "22222222-2222-2222-2222-222222222222";

  @Container static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @DynamicPropertySource
  static void mongoProps(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
  }

  @Autowired private NoteRepository notes;

  private Note save(String userId, String title, String content, List<String> tags) {
    return notes.save(Note.create(userId, title, content, tags));
  }

  @Test
  void listaDoUsuarioOrdenadaPorModificacaoDesc() throws InterruptedException {
    notes.deleteAll();
    save(USER, "Antiga", "c", List.of());
    Thread.sleep(20); // garante updatedAt distinto (precisao de milissegundos no Mongo)
    save(USER, "Recente", "c", List.of());
    save(OTHER, "DeOutro", "c", List.of());

    List<Note> result = notes.findByUserIdOrderByUpdatedAtDesc(USER);

    assertThat(result).extracting(Note::getTitle).containsExactly("Recente", "Antiga");
  }

  @Test
  void findByIdAndUserIdEscopaPorDono() {
    notes.deleteAll();
    Note nota = save(USER, "Privada", "c", List.of());

    assertThat(notes.findByIdAndUserId(nota.getId(), USER)).isPresent();
    assertThat(notes.findByIdAndUserId(nota.getId(), OTHER)).isEmpty();
  }

  @Test
  void searchSemFiltrosRetornaTodasDoUsuario() {
    notes.deleteAll();
    save(USER, "A", "c", List.of());
    save(USER, "B", "c", List.of());
    save(OTHER, "C", "c", List.of());

    assertThat(notes.search(USER, null, null)).extracting(Note::getTitle).containsExactly("B", "A");
  }

  @Test
  void searchPorTagFiltraEEscopaPorUsuario() {
    notes.deleteAll();
    save(USER, "Trabalho", "reuniao", List.of("work", "urgente"));
    save(USER, "Pessoal", "academia", List.of("home"));
    save(OTHER, "Alheia", "x", List.of("work"));

    assertThat(notes.search(USER, "work", null))
        .extracting(Note::getTitle)
        .containsExactly("Trabalho");
  }

  @Test
  void searchPorTextoCaseInsensitiveEmTituloOuConteudo() {
    notes.deleteAll();
    save(USER, "Compras", "Comprar LEITE e pao", List.of());
    save(USER, "LEITE materno", "outro assunto", List.of());
    save(USER, "Treino", "corrida", List.of());

    assertThat(notes.search(USER, null, "leite"))
        .extracting(Note::getTitle)
        .containsExactlyInAnyOrder("Compras", "LEITE materno");
  }
}
