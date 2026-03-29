package org.synapse.domain.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Note {
    private final NoteId id;
    private String title;
    private String content;
    private final Path filePath;
    private final Set<Tag> tags;
    private final Set<Link> links;
    private final Set<Link> backlinks;
    private final Instant createdAt;
    private Instant lastAccessedAt;
    private NoteState state;

    private Note(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.content = builder.content;
        this.filePath = builder.filePath;
        this.tags = new HashSet<>(builder.tags);
        this.links = new HashSet<>(builder.links);
        this.backlinks = new HashSet<>(builder.backlinks);
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.lastAccessedAt = builder.lastAccessedAt != null ? builder.lastAccessedAt : this.createdAt;
        this.state = builder.state != null ? builder.state : NoteState.DORMANT;
    }

    // Getters
    public NoteId getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Path getFilePath() { return filePath; }
    public Set<Tag> getTags() { return Collections.unmodifiableSet(tags); }
    public Set<Link> getLinks() { return Collections.unmodifiableSet(links); }
    public Set<Link> getBacklinks() { return Collections.unmodifiableSet(backlinks); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public NoteState getState() { return state; }

    // Domain methods
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
        markAccessed();
    }

    public void markAccessed() {
        this.lastAccessedAt = Instant.now();
    }

    public void updateState(NoteState newState) {
        this.state = newState;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NoteId id;
        private String title;
        private String content;
        private Path filePath;
        private Set<Tag> tags = new HashSet<>();
        private Set<Link> links = new HashSet<>();
        private Set<Link> backlinks = new HashSet<>();
        private Instant createdAt;
        private Instant lastAccessedAt;
        private NoteState state;

        public Builder id(NoteId id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder filePath(Path filePath) { this.filePath = filePath; return this; }
        public Builder tags(Set<Tag> tags) { this.tags = tags != null ? tags : new HashSet<>(); return this; }
        public Builder links(Set<Link> links) { this.links = links != null ? links : new HashSet<>(); return this; }
        public Builder backlinks(Set<Link> backlinks) { this.backlinks = backlinks != null ? backlinks : new HashSet<>(); return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; return this; }
        public Builder state(NoteState state) { this.state = state; return this; }

        public Note build() {
            if (id == null) throw new IllegalStateException("NoteId is required");
            if (title == null) throw new IllegalStateException("Title is required");
            if (content == null) throw new IllegalStateException("Content is required");
            if (filePath == null) throw new IllegalStateException("FilePath is required");
            return new Note(this);
        }
    }
}
