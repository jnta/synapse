4. Phase 4: Explicit Graph & Vector Engine
The Right Panel now displays the direct neighbors of your current note in the graph.

[x] Graph-Link Engine (SQLDelight):

[x] Implement getLinkedNotes(noteId): A query that joins Notes and Edges to find all Forward-links and Back-links.

[x] Linked-Notes Sidebar:

[x] Forward-Links Section: Notes that the current note references via [[Link]].

[x] Back-Links Section: Notes that reference the current note.

[x] Semantic Discovery (The "Inspiration" layer):

[x] Integrate ObjectBox Vector Search + ONNX Runtime. (Integrated ONNX Runtime + ResonanceRepository with local embedding generation).

[x] Implement a toggle or a "Discovery" tab within the sidebar to show unlinked but semantically similar notes (to help you create new links).

[x] GraphFocusWidget: A Canvas visualization centered on the current note, drawing lines only to the explicitly linked notes in the sidebar.

5. Phase 5: Metadata Persistence & Graph Interaction
Formalizing the data and the keyboard-driven "Overflow" flow.

[ ] Metadata Persistence Layer (SQLDelight):

[ ] Create the NoteAttributes table (as specified in your schema).

[ ] Implement an Attribute Parser: Detect key::value or inline tags and update the database in real-time.

[ ] Dynamic Card UI (The Linked Cards):

[ ] Implement Meta-Pills on the cards in the Right Panel. For example, if a Back-link is marked as #Evergreen, the pill should reflect that status immediately.

[ ] The "Note Overflow" (Transvazar) System:

[ ] Keyboard Shortcut: Ctrl+Enter.

[ ] Logic: Extract Block -> Create New Note -> Link to Parent -> Inherit Metadata.

[ ] Power-User Navigation:

[ ] Alt + Click on Card: Opens the linked note while maintaining the Breadcrumb history.

[ ] Ctrl + I: Explicitly triggers the "Discover Similar" (Vector Search) to suggest new links for the sidebar.

Updated Schema for Graph Relations
To support the "Related Notes" panel (Links), you need a specific table for the edges:

SQL
CREATE TABLE NoteEdges (
    id TEXT PRIMARY KEY,
    source_id TEXT NOT NULL,
    target_id TEXT NOT NULL,
    label TEXT, -- e.g., 'supports', 'refutes', or NULL for general link
    FOREIGN KEY(source_id) REFERENCES Notes(id) ON DELETE CASCADE,
    FOREIGN KEY(target_id) REFERENCES Notes(id) ON DELETE CASCADE
);

-- Fast lookup for Backlinks
CREATE INDEX idx_note_edges_target ON NoteEdges(target_id);