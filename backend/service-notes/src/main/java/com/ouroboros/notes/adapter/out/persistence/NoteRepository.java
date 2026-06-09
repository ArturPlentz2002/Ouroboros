package com.ouroboros.notes.adapter.out.persistence;

import com.ouroboros.notes.domain.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoteRepository extends MongoRepository<Note, String> {

  List<Note> findByUserIdOrderByUpdatedAtDesc(String userId);

  Optional<Note> findByIdAndUserId(String id, String userId);
}
