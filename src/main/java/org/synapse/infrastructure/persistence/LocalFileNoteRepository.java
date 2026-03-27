package org.synapse.infrastructure.persistence;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;
import org.synapse.domain.repository.NoteRepository;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class LocalFileNoteRepository implements NoteRepository {

    private final Path vaultPath;

    public LocalFileNoteRepository(@ConfigProperty(name = "synapse.vault.path", defaultValue = "${user.home}/.synapse/vault") String vaultPathStr) {
        this.vaultPath = Path.of(vaultPathStr);
        try {
            Files.createDirectories(this.vaultPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize vault directory: " + this.vaultPath, e);
        }
    }

    @Override
    public List<Note> findAll() {
        try (Stream<Path> paths = Files.walk(vaultPath, 1)) {
            return paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .map(this::readNoteFromFile)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        Path filePath = vaultPath.resolve(id.value() + ".md");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        return readNoteFromFile(filePath);
    }

    @Override
    public void save(Note note) {
        Path filePath = vaultPath.resolve(note.getId().value() + ".md");
        try {
            Files.writeString(filePath, note.getContent());
        } catch (IOException e) {
            throw new RuntimeException("Could not write note to file: " + filePath, e);
        }
    }

    @Override
    public void delete(NoteId id) {
        Path filePath = vaultPath.resolve(id.value() + ".md");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete note file: " + filePath, e);
        }
    }

    private Optional<Note> readNoteFromFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            String fileName = filePath.getFileName().toString();
            String idStr = fileName.substring(0, fileName.lastIndexOf('.'));
            
            String title = idStr;
            if (content.startsWith("# ")) {
                int endOfLine = content.indexOf('\n');
                if (endOfLine > 0) {
                    title = content.substring(2, endOfLine).trim();
                } else {
                    title = content.substring(2).trim();
                }
            }
            
            Note note = Note.builder()
                    .id(new NoteId(idStr))
                    .title(title)
                    .content(content)
                    .filePath(filePath)
                    .build();
            return Optional.of(note);
        } catch (IOException e) {
            System.err.println("Could not read note file: " + filePath);
            return Optional.empty();
        }
    }
}
