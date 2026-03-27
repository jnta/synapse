package org.synapse.domain.model;

public record Link(NoteId source, NoteId target, LinkLabel label) {
    public Link {
        if (source == null) {
            throw new IllegalArgumentException("Source NoteId cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target NoteId cannot be null");
        }
    }
}
