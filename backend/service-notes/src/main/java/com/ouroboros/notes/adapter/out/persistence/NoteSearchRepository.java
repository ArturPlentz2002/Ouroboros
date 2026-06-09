package com.ouroboros.notes.adapter.out.persistence;

import com.ouroboros.notes.domain.Note;
import java.util.List;

/** Busca dinamica de notas (fragmento custom do {@link NoteRepository}). */
public interface NoteSearchRepository {

  /**
   * Lista as notas do usuario aplicando filtros opcionais: {@code tag} (igualdade num elemento do
   * array de tags) e {@code text} (regex case-insensitive em titulo ou conteudo). Ordena pela data
   * de modificacao desc.
   */
  List<Note> search(String userId, String tag, String text);
}
