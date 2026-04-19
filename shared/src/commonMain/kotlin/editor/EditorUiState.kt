package editor

import androidx.compose.runtime.Immutable
import editor.ast.NoteNode
import editor.ast.AstParser
import dev.synapse.domain.model.Note




@Immutable
data class NoteBlock(
    val id: String,
    val content: String,
    val detectedAttributes: List<String> = emptyList(),
    val astNode: NoteNode.BlockNode // Keeping for rendering logic
)

@Immutable
data class ResonanceItem(
    val id: String,
    val title: String,
    val snippet: String,
    val tags: List<String> = emptyList()
)

@Immutable
data class EditorUiState(
    val noteId: String = "",
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val navigationStack: List<String> = emptyList(), // Session Breadcrumbs
    val blocks: List<NoteBlock> = emptyList(),
    val forwardLinks: List<Note> = emptyList(),
    val backLinks: List<Note> = emptyList(),
    val resonanceItems: List<ResonanceItem> = emptyList(),
    val selectionMetadata: Map<String, String> = emptyMap(), // Dynamic attributes (e.g., "status" to "evergreen")
    val focusedBlockId: String? = null,
    val isSidebarVisible: Boolean = true,
    val isContextPanelVisible: Boolean = true,
    val showResonanceFilter: Boolean = false,
    val originalThought: String = "",
    val minThoughtLength: Int = 50
)

sealed interface EditorUiEvent {
    data object LoadNotes : EditorUiEvent
    data class SelectNote(val noteId: String) : EditorUiEvent
    data class MapsTo(val noteId: String) : EditorUiEvent // Phase 2 requirement
    
    // Block events
    data class UpdateBlockContent(val blockId: String, val newContent: String) : EditorUiEvent
    data class AddBlockAfter(val blockId: String) : EditorUiEvent
    data class RemoveBlock(val blockId: String) : EditorUiEvent
    data class MoveBlock(val fromIndex: Int, val toIndex: Int) : EditorUiEvent
    data class FocusBlock(val blockId: String?) : EditorUiEvent

    data object RequestResonance : EditorUiEvent // Phase 2 requirement
    data object CreateNewNote : EditorUiEvent
    data object ToggleSidebar : EditorUiEvent
    data object ToggleContextPanel : EditorUiEvent
    data object SaveCurrentNote : EditorUiEvent 
    data object TriggerSearch : EditorUiEvent
    
    // Resonance Filter
    data class UpdateOriginalThought(val text: String) : EditorUiEvent
    data object CommitNote : EditorUiEvent
}
