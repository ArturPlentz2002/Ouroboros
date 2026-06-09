package com.ouroboros.notes.application;

import com.ouroboros.notes.adapter.out.persistence.NoteRepository;
import com.ouroboros.notes.domain.Note;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Casos de uso de notas. Todas as operacoes sao escopadas pelo usuario dono. */
@Service
public class NoteService {

  private final NoteRepository notes;

  public NoteService(NoteRepository notes) {
    this.notes = notes;
  }

  public Note create(String userId, String title, String content, List<String> tags) {
    return notes.save(Note.create(userId, title, content, tags));
  }

  public List<Note> list(String userId) {
    return notes.findByUserIdOrderByUpdatedAtDesc(userId);
  }

  /**
   * Lista as notas do usuario, opcionalmente filtrando por {@code tag} e/ou {@code text}. Sem
   * filtros, recai na listagem completa.
   */
  public List<Note> search(String userId, String tag, String text) {
    if (!StringUtils.hasText(tag) && !StringUtils.hasText(text)) {
      return list(userId);
    }
    return notes.search(userId, tag, text);
  }

  public Note get(String userId, String id) {
    return notes.findByIdAndUserId(id, userId).orElseThrow(() -> new NoteNotFoundException(id));
  }

  public Note update(String userId, String id, String title, String content, List<String> tags) {
    Note note =
        notes.findByIdAndUserId(id, userId).orElseThrow(() -> new NoteNotFoundException(id));
    note.update(title, content, tags);
    return notes.save(note);
  }

  public void delete(String userId, String id) {
    Note note =
        notes.findByIdAndUserId(id, userId).orElseThrow(() -> new NoteNotFoundException(id));
    notes.delete(note);
  }
}
