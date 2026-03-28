package org.synapse.infrastructure.persistence;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;
import org.synapse.domain.repository.NoteRepository;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class LocalFileNoteRepository implements NoteRepository {

    private static final Logger LOG = Logger.getLogger(LocalFileNoteRepository.class);
    private static final String MD_EXTENSION = ".md";
    private static final String MDC_NOTE_ID = "noteId";

    private final Path vaultPath;
    private final AtomicFileStore fileStore;

    public LocalFileNoteRepository(
            @ConfigProperty(name = "synapse.vault.path", defaultValue = "${user.home}/.synapse/vault") String vaultPathStr,
            AtomicFileStore fileStore) {
        this.vaultPath = Path.of(vaultPathStr);
        this.fileStore = fileStore;
        initializeVault();
    }

    private void initializeVault() {
        try {
            Files.createDirectories(this.vaultPath);
            LOG.info("Initialize Vault SUCCESS");
        } catch (IOException e) {
            LOG.error("Initialize Vault ERROR: " + this.vaultPath, e);
            throw new RuntimeException("Could not initialize vault directory: " + this.vaultPath, e);
        }
    }

    @Override
    public List<Note> findAll() {
        LOG.debug("List Notes START");
        try {
            if (!Files.exists(vaultPath)) return List.of();
            try (Stream<Path> paths = Files.walk(vaultPath, 10)) {
                List<Note> notes = paths.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(MD_EXTENSION))
                            .map(this::readNoteFromFile)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                LOG.infof("List Notes SUCCESS count=%d", notes.size());
                return notes;
            }
        } catch (IOException e) {
            LOG.error("List Notes ERROR", e);
            return List.of();
        }
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        MDC.put(MDC_NOTE_ID, id.value());
        try {
            Path filePath = getFilePath(id);
            if (!Files.exists(filePath)) {
                LOG.debug("Find Note MISS");
                return Optional.empty();
            }
            Optional<Note> note = readNoteFromFile(filePath);
            LOG.debug("Find Note SUCCESS");
            return note;
        } finally {
            MDC.remove(MDC_NOTE_ID);
        }
    }

    @Override
    public void save(Note note) {
        MDC.put(MDC_NOTE_ID, note.getId().value());
        try {
            long start = System.currentTimeMillis();
            fileStore.writeAtomic(getFilePath(note.getId()), note.getContent());
            long duration = System.currentTimeMillis() - start;
            LOG.infof("Save Note SUCCESS duration_ms=%d", duration);
        } catch (IOException e) {
            LOG.error("Save Note ERROR", e);
            throw new RuntimeException("Could not save note: " + note.getId().value(), e);
        } finally {
            MDC.remove(MDC_NOTE_ID);
        }
    }

    private Path getFilePath(NoteId id) {
        return vaultPath.resolve(id.value() + MD_EXTENSION);
    }

    @Override
    public void delete(NoteId id) {
        MDC.put(MDC_NOTE_ID, id.value());
        try {
            Path filePath = getFilePath(id);
            Files.deleteIfExists(filePath);
            LOG.info("Delete Note SUCCESS");
        } catch (IOException e) {
            LOG.error("Delete Note ERROR", e);
            throw new RuntimeException("Could not delete note file: " + id.value(), e);
        } finally {
            MDC.remove(MDC_NOTE_ID);
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
        Path possibleSourceFile = vaultPath.resolve(sourceId + MD_EXTENSION);
        Path possibleSourceFolder = vaultPath.resolve(sourceId);

        Path actualSource;
        Path actualTarget;

        if (Files.isRegularFile(possibleSourceFile)) {
            actualSource = possibleSourceFile;
            actualTarget = vaultPath.resolve(targetId + MD_EXTENSION);
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
