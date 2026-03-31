package org.synapse.application.usecase;

import org.synapse.application.dto.CreateNoteCommand;
import org.synapse.application.dto.NoteDTO;
import org.synapse.application.dto.UpdateNoteCommand;
import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;
import org.synapse.domain.repository.NoteRepository;
import org.synapse.domain.service.SlugGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class NoteUseCases {

    private final NoteRepository noteRepository;
    private final SlugGenerator slugGenerator;

    public NoteUseCases(NoteRepository noteRepository, SlugGenerator slugGenerator) {
        this.noteRepository = noteRepository;
        this.slugGenerator = slugGenerator;
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
        String slug = slugGenerator.generateSlug(command.title());
        NoteId newId = new NoteId(slug);
        
        // Ensure uniqueness
        int counter = 1;
        while (noteRepository.findById(newId).isPresent()) {
            newId = new NoteId(slug + "-" + counter);
            counter++;
        }

        Path logicalPath = Path.of(newId.value() + ".md");

        String content = command.content();
        if (content == null) content = "";
        if (content.startsWith("# ")) {
            int firstNewline = content.indexOf('\n');
            if (firstNewline > 0) {
                content = "# " + command.title() + content.substring(firstNewline);
            } else {
                content = "# " + command.title();
            }
        } else {
            content = "# " + command.title() + "\n\n" + content;
        }

        Note note = Note.builder()
                .id(newId)
                .title(command.title())
                .content(content)
                .filePath(logicalPath)
                .build();

        noteRepository.save(note);
        return NoteDTO.from(note);
    }

    public NoteDTO updateNote(String id, UpdateNoteCommand command) {
        Note note = noteRepository.findById(new NoteId(id))
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + id));

        String content = command.content();
        if (content != null) {
            if (content.startsWith("# ")) {
                int firstNewline = content.indexOf('\n');
                if (firstNewline > 0) {
                    content = "# " + command.title() + content.substring(firstNewline);
                } else {
                    content = "# " + command.title();
                }
            } else {
                content = "# " + command.title() + "\n\n" + content;
            }
        }

        note.updateContent(command.title(), content);
        noteRepository.save(note);
        return NoteDTO.from(note);
    }

    public void deleteNote(String id) {
        noteRepository.delete(new NoteId(id));
    }

    public void createFolder(String path) {
        noteRepository.createFolder(path);
    }

    public List<String> getAllFolders() {
        return noteRepository.findAllFolders();
    }

    public void moveNode(String source, String target) {
        noteRepository.move(source, target);
    }
}
