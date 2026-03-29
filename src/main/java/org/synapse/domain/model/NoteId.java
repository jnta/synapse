package org.synapse.domain.model;

public record NoteId(String value) {
    public NoteId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NoteId value cannot be null or blank");
        }
    }
}
