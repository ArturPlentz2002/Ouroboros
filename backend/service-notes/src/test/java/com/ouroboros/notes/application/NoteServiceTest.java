package com.ouroboros.notes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.notes.adapter.out.persistence.NoteRepository;
import com.ouroboros.notes.domain.Note;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

  private static final String USER = "11111111-1111-1111-1111-111111111111";
  private static final String OTHER = "22222222-2222-2222-2222-222222222222";

  @Mock private NoteRepository notes;
  @InjectMocks private NoteService service;

  @Test
  void criaPersisteNotaDoUsuario() {
    when(notes.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

    Note created = service.create(USER, "Titulo", "Corpo", List.of("a", "b"));

    assertThat(created.getUserId()).isEqualTo(USER);
    assertThat(created.getTitle()).isEqualTo("Titulo");
    assertThat(created.getTags()).containsExactly("a", "b");
    assertThat(created.getId()).isNotBlank();
    verify(notes).save(any(Note.class));
  }

  @Test
  void listaDelegaAoRepositorioEscopadoPorUsuario() {
    Note n = Note.create(USER, "T", "C", List.of());
    when(notes.findByUserIdOrderByUpdatedAtDesc(USER)).thenReturn(List.of(n));

    assertThat(service.list(USER)).containsExactly(n);
  }

  @Test
  void getRetornaNotaQuandoEncontrada() {
    Note n = Note.create(USER, "T", "C", List.of());
    when(notes.findByIdAndUserId(n.getId(), USER)).thenReturn(Optional.of(n));

    assertThat(service.get(USER, n.getId())).isEqualTo(n);
  }

  @Test
  void getLancaQuandoNaoEncontrada() {
    when(notes.findByIdAndUserId("x", OTHER)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(OTHER, "x")).isInstanceOf(NoteNotFoundException.class);
  }

  @Test
  void atualizaModificaCamposEPersiste() {
    Note n = Note.create(USER, "Antigo", "C", List.of("x"));
    when(notes.findByIdAndUserId(n.getId(), USER)).thenReturn(Optional.of(n));
    when(notes.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

    Note updated = service.update(USER, n.getId(), "Novo", "C2", List.of("y"));

    assertThat(updated.getTitle()).isEqualTo("Novo");
    assertThat(updated.getContent()).isEqualTo("C2");
    assertThat(updated.getTags()).containsExactly("y");
  }

  @Test
  void atualizaLancaQuandoNaoEncontrada() {
    when(notes.findByIdAndUserId("x", USER)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(USER, "x", "T", "C", List.of()))
        .isInstanceOf(NoteNotFoundException.class);
    verify(notes, never()).save(any());
  }

  @Test
  void deletaRemoveNotaDoUsuario() {
    Note n = Note.create(USER, "T", "C", List.of());
    when(notes.findByIdAndUserId(n.getId(), USER)).thenReturn(Optional.of(n));

    service.delete(USER, n.getId());

    verify(notes).delete(n);
  }

  @Test
  void deletaLancaQuandoNaoEncontrada() {
    when(notes.findByIdAndUserId("x", USER)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(USER, "x")).isInstanceOf(NoteNotFoundException.class);
    verify(notes, never()).delete(any());
  }
}
