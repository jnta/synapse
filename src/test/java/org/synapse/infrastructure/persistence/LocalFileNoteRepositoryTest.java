package org.synapse.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileNoteRepositoryTest {

    private Path tempVault;
    private LocalFileNoteRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        tempVault = Files.createTempDirectory("synapse-test-vault");
        repository = new LocalFileNoteRepository(tempVault.toString(), new AtomicFileStore());
    }

    @AfterEach
    void tearDown() throws IOException {
        try (var stream = Files.walk(tempVault)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        }
    }

    @Test
    void testSaveAndFind() {
        Note note = Note.builder()
                .id(new NoteId("test-id"))
                .title("Test Note")
                .content("# Test Note\nHello World")
                .filePath(tempVault.resolve("test-id.md"))
                .build();

        repository.save(note);

        Optional<Note> found = repository.findById(new NoteId("test-id"));
        assertTrue(found.isPresent());
        assertEquals("Test Note", found.get().getTitle());
        assertEquals("# Test Note\nHello World", found.get().getContent());
    }

    @Test
    void testFindAll() {
        Note note1 = Note.builder().id(new NoteId("1")).title("N1").content("# N1").filePath(tempVault.resolve("1.md")).build();
        Note note2 = Note.builder().id(new NoteId("2")).title("N2").content("# N2").filePath(tempVault.resolve("2.md")).build();
        
        repository.save(note1);
        repository.save(note2);

        List<Note> all = repository.findAll();
        assertEquals(2, all.size());
    }
}
