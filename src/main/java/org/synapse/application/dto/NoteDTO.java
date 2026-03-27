package org.synapse.application.dto;

import org.synapse.domain.model.Note;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record NoteDTO(
        String id,
        String title,
        String content,
        Set<String> tags,
        Instant createdAt,
        Instant lastAccessedAt,
        String state
) {
    public static NoteDTO from(Note note) {
        return new NoteDTO(
                note.getId().value(),
                note.getTitle(),
                note.getContent(),
                note.getTags().stream().map(t -> t.name()).collect(Collectors.toSet()),
                note.getCreatedAt(),
                note.getLastAccessedAt(),
                note.getState().name()
        );
    }
}
