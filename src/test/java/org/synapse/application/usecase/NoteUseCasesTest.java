package org.synapse.application.usecase;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.synapse.application.dto.CreateNoteCommand;
import org.synapse.application.dto.NoteDTO;
import org.synapse.application.dto.UpdateNoteCommand;
import org.synapse.domain.model.Note;
import org.synapse.domain.model.NoteId;
import org.synapse.domain.repository.NoteRepository;
import org.synapse.domain.service.SlugGenerator;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NoteUseCasesTest {

    private final NoteRepository mockRepository = Mockito.mock(NoteRepository.class);
    private final SlugGenerator slugGenerator = new SlugGenerator();
    private final NoteUseCases useCases = new NoteUseCases(mockRepository, slugGenerator);

    @Test
    void testCreateNote() {
        CreateNoteCommand cmd = new CreateNoteCommand("Test Title", "Test Content");
        NoteDTO created = useCases.createNote(cmd);

        assertNotNull(created.id());
        assertEquals("Test Title", created.title());
        assertEquals("Test Content", created.content());
        verify(mockRepository).save(any(Note.class));
    }

    @Test
    void testUpdateNote() {
        Note existingNote = Note.builder()
                .id(new NoteId("existing-id"))
                .title("Old Title")
                .content("Old Content")
                .filePath(Path.of("existing-id.md"))
                .build();
        
        when(mockRepository.findById(new NoteId("existing-id"))).thenReturn(Optional.of(existingNote));

        UpdateNoteCommand cmd = new UpdateNoteCommand("New Title", "New Content");
        NoteDTO updated = useCases.updateNote("existing-id", cmd);

        assertEquals("New Title", updated.title());
        assertEquals("New Content", updated.content());
        verify(mockRepository).save(existingNote);
    }
}
