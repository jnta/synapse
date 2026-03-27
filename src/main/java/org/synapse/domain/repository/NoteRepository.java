package org.synapse.domain.repository;

import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;

import java.util.List;
import java.util.Optional;

public interface NoteRepository {
    List<Note> findAll();
    Optional<Note> findById(NoteId id);
    void save(Note note);
    void delete(NoteId id);
}
