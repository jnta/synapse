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
        try (Stream<Path> paths = Files.walk(vaultPath)) {
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
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
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

    @Override
    public void createFolder(String path) {
        Path folderPath = vaultPath.resolve(path);
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create folder: " + folderPath, e);
        }
    }

    @Override
    public List<String> findAllFolders() {
        try (Stream<Path> paths = Files.walk(vaultPath)) {
            return paths.filter(Files::isDirectory)
                        .filter(p -> !p.equals(vaultPath))
                        .map(p -> vaultPath.relativize(p).toString().replace('\\', '/'))
                        .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public void move(String sourceId, String targetId) {
        Path possibleSourceFile = vaultPath.resolve(sourceId + ".md");
        Path possibleSourceFolder = vaultPath.resolve(sourceId);

        Path actualSource;
        Path actualTarget;

        if (Files.isRegularFile(possibleSourceFile)) {
            actualSource = possibleSourceFile;
            actualTarget = vaultPath.resolve(targetId + ".md");
        } else if (Files.isDirectory(possibleSourceFolder)) {
            actualSource = possibleSourceFolder;
            actualTarget = vaultPath.resolve(targetId);
        } else {
            throw new IllegalArgumentException("Source does not exist: " + sourceId);
        }

        try {
            if (actualTarget.getParent() != null) {
                Files.createDirectories(actualTarget.getParent());
            }
            Files.move(actualSource, actualTarget);
        } catch (IOException e) {
            throw new RuntimeException("Could not move node", e);
        }
    }

    private Optional<Note> readNoteFromFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            Path relativePath = vaultPath.relativize(filePath);
            String relativeStr = relativePath.toString().replace('\\', '/');
            String idStr = relativeStr.substring(0, relativeStr.lastIndexOf('.'));
            
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
