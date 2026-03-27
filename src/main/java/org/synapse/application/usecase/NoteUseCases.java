package org.synapse.application.usecase;

import org.synapse.application.dto.CreateNoteCommand;
import org.synapse.application.dto.NoteDTO;
import org.synapse.application.dto.UpdateNoteCommand;
import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;
import org.synapse.domain.repository.NoteRepository;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class NoteUseCases {

    private final NoteRepository noteRepository;

    public NoteUseCases(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<NoteDTO> getAllNotes() {
        return noteRepository.findAll().stream()
                .map(NoteDTO::from)
                .collect(Collectors.toList());
    }

    public NoteDTO getNote(String id) {
        return noteRepository.findById(new NoteId(id))
                .map(NoteDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + id));
    }

    public NoteDTO createNote(CreateNoteCommand command) {
        NoteId newId = new NoteId(UUID.randomUUID().toString());
        Path logicalPath = Path.of(newId.value() + ".md");

        Note note = Note.builder()
                .id(newId)
                .title(command.title())
                .content(command.content())
                .filePath(logicalPath)
                .build();

        noteRepository.save(note);
        return NoteDTO.from(note);
    }

    public NoteDTO updateNote(String id, UpdateNoteCommand command) {
        Note note = noteRepository.findById(new NoteId(id))
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + id));

        note.updateContent(command.title(), command.content());
        noteRepository.save(note);
        return NoteDTO.from(note);
    }

    public void deleteNote(String id) {
        noteRepository.delete(new NoteId(id));
    }
}
