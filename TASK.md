Task: Implement Synapse Refined Editor & Relation Grid
This task covers the core UI implementation of the Synapse workspace, focusing on the Atomic Writing flow, the intent-based block system, and the lateral resonance visualization.

1. Phase 1: Global Shell & Layout Infrastructure (DONE)
The goal is to establish the three-column architecture before focusing on internal logic.

[x] MainLayoutContainer: Implement a three-column scaffold using Row with independent scroll behaviors.

[x] LeftNav (SideBar): Fixed width. Implement NavBrand ("SYNAPSE") and PerspectiveList (Filter anchors: #Captura, #Referência, #Síntese, #Mapa).

[x] EditorViewport (Center): Fluid width with a max-width: 800px constraint for the content area.

[x] ContextPanel (Right): Fixed width sidebar, toggleable visibility.

[x] NavigationState: Implement the BreadcrumbTrail at the top of the Center Viewport. It must observe the navigationStack to render the session history as clickable breadcrumbs.

2. Phase 2: State Management & Event Schema (commonMain) (DONE)
Refactored Phase 2: State Management & Event Schema
Moving from a rigid Enum to an Attribute-based Graph model.

[x] EditorUiState Definition:

Kotlin
data class EditorUiState(
    val noteId: String = "",
    val navigationStack: List<String> = emptyList(), // Session Breadcrumbs
    val blocks: List<NoteBlock> = emptyList(),
    val resonanceItems: List<ResonanceItem> = emptyList(),
    val selectionMetadata: Map<String, String> = emptyMap() // Dynamic attributes (e.g., "status" to "evergreen")
)
[x] Dynamic NoteBlock Model:

Kotlin
data class NoteBlock(
    val id: String,
    val content: String,
    val detectedAttributes: List<String> = emptyList() // Extracted from [C], [R], [S] or AI
)
[x] Event Handling: Implement EditorUiEvent for MapsTo(noteId), RequestResonance, and UpdateBlockContent.

3. Phase 3: Atomic Block Editor (The Core)
Building the individual units of thought and their interaction logic.

[ ] LazyBlockList: Use a LazyColumn to render NoteBlock entities. Use key for performance optimization.

[ ] EditorBlock Component:

[ ] BlockGutter: Implement a slot for the IntentIndicator (dynamic icon based on syntax) and the drag_indicator.

[ ] BlockTextField: Integrate BasicTextField with a custom VisualTransformation for real-time Markdown styling.

[ ] Focus Management: Implement FocusRequester logic so Enter creates a new block and automatically moves focus, while Backspace merges empty blocks.

4. Phase 4: Resonance Panel & Contextual Widgets
Adding the intelligence layer that reacts to the written content.

[ ] ResonanceCards: Implement the right-sidebar list.

[ ] Cards must show: Title -> 3-line Snippet -> Status Pills (Tags) at the base.

[ ] Implement EmptyResonanceState (Instructions: "Press Ctrl+I to resonate").

[ ] GraphFocusWidget: Build a Canvas-based circular visualization in the right sidebar to indicate the "link weight" of the current note.

[ ] VibeIndicator: Add a subtle text-metrics indicator in the editor footer (DNA Editorial feedback).

5. Phase 5: Interaction Refinement & Keyboard Shortcuts
Polishing for the power-user/TWM experience.

[ ] Keyboard Shortcuts: Map Ctrl+I (Trigger Resonance), Ctrl+O (HUD Capture), and Ctrl+Enter (Note Overflow/Transbordo).

[ ] Desktop UX: Implement hover states for the drag handles and smooth lateral "Slide" animations when navigating through the breadcrumb stack.

[ ] Metadata Parser: Logic to automatically detect tags or attributes at the start of a block to update the UI state.

Implementation Verification
Performance: Ensure no full-list recompositions during typing.

Layout: Breadcrumbs must be the primary way to navigate back (no tabs).

Responsiveness: Columns must behave predictably in different window sizes without breaking the 800px editor limit.