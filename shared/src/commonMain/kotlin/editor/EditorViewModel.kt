package editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlin.random.Random
import dev.synapse.domain.repository.NoteRepository
import dev.synapse.domain.model.Note
import dev.synapse.domain.util.NoteParser
import dev.synapse.domain.model.Attribute
import dev.synapse.domain.model.Edge
import kotlinx.coroutines.flow.collect

private const val DEBOUNCE_MS = 300L
private const val AUTO_SAVE_DEBOUNCE_MS = 2000L
private const val MAX_TITLE_LENGTH = 50

@OptIn(FlowPreview::class)
class EditorViewModel(
    private val coroutineScope: CoroutineScope,
    private val repository: NoteRepository
) {


    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val textChangeFlow = MutableSharedFlow<Pair<String, String>>(
        extraBufferCapacity = 64
    )

    init {
        onEvent(EditorUiEvent.LoadNotes)
        
        coroutineScope.launch {
            repository.getAllNotes().collect { notes ->
                _state.update { it.copy(notes = notes, isLoading = false) }
            }
        }

        coroutineScope.launch {
            textChangeFlow
                .debounce(DEBOUNCE_MS)
                .collectLatest { (blockId, content) ->
                    parseBlockBackground(blockId, content)
                }
        }

        coroutineScope.launch {
            textChangeFlow
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collectLatest { 
                    handleSave(isCommit = false, isAutoSave = true) 
                }
        }
    }

    fun onEvent(event: EditorUiEvent) {
        if (!handleNoteEvent(event)) {
            handleBlockEvent(event)
        }
    }

    private fun handleBlockEvent(event: EditorUiEvent) {
        when (event) {
            is EditorUiEvent.UpdateBlockContent -> updateBlockContent(event.blockId, event.newContent)
            is EditorUiEvent.AddBlockAfter -> mutateBlocks(BlockAction.Add(event.blockId))
            is EditorUiEvent.RemoveBlock -> mutateBlocks(BlockAction.Remove(event.blockId))
            is EditorUiEvent.MoveBlock -> mutateBlocks(BlockAction.Move(event.fromIndex, event.toIndex))
            is EditorUiEvent.FocusBlock -> _state.update { state -> state.copy(focusedBlockId = event.blockId) }
            else -> {}
        }
    }

    private fun handleNoteEvent(event: EditorUiEvent): Boolean {
        return when (event) {
            is EditorUiEvent.SelectNote -> { selectNote(event.noteId); true }
            is EditorUiEvent.MapsTo -> { selectNote(event.noteId); true }
            is EditorUiEvent.RequestResonance -> { 
                val mockResonance = listOf(
                    ResonanceItem(
                        EditorLogic.generateId(), 
                        "Related Concept", 
                        "Snippet of a related thought...", 
                        listOf("Theory")
                    ),
                    ResonanceItem(
                        EditorLogic.generateId(), 
                        "Contradictory Note", 
                        "This contradicts the current hypothesis...", 
                        listOf("Fact", "Critical")
                    )
                )
                _state.update { it.copy(resonanceItems = mockResonance) }
                true 
            }
            is EditorUiEvent.CreateNewNote -> { launchCreateNote(); true }
            is EditorUiEvent.SaveCurrentNote -> { handleSave(isCommit = false); true }
            is EditorUiEvent.CommitNote -> { handleSave(isCommit = true); true }
            is EditorUiEvent.UpdateOriginalThought -> { 
                _state.update { it.copy(originalThought = event.text) }; true 
            }
            is EditorUiEvent.ToggleSidebar -> { 
                _state.update { it.copy(isSidebarVisible = !it.isSidebarVisible) }; true 
            }
            is EditorUiEvent.ToggleContextPanel -> { 
                _state.update { it.copy(isContextPanelVisible = !it.isContextPanelVisible) }; true 
            }
            else -> false
        }
    }

    private fun handleSave(isCommit: Boolean, isAutoSave: Boolean = false) {
        val currentThought = _state.value.originalThought.length
        val minThought = _state.value.minThoughtLength

        if (isCommit && currentThought < minThought) {
            _state.update { it.copy(showResonanceFilter = true) }
            return
        }

        val selectedId = if (_state.value.noteId.isEmpty()) null else _state.value.noteId
        if (selectedId == null) return
        val fullContent = _state.value.blocks.joinToString("\n\n") { it.content }
        
        val derivedTitle = _state.value.blocks.firstOrNull()?.content
            ?.lineSequence()?.firstOrNull()?.removePrefix("# ")?.take(MAX_TITLE_LENGTH) ?: "Untitled"

        coroutineScope.launch {
            val existingNote = repository.getNoteById(selectedId)
            val updatedNote = Note(
                id = selectedId,
                title = derivedTitle,
                content = fullContent,
                attributes = NoteParser.extractAttributes(fullContent),
                connections = NoteParser.extractEdges(selectedId, fullContent),
                createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(updatedNote)
            
            if (!isAutoSave) {
                _state.update { it.copy(originalThought = "", showResonanceFilter = false) }
            }
        }
    }



    private fun selectNote(noteId: String) {
        coroutineScope.launch {
            val note = repository.getNoteById(noteId) ?: return@launch
            
            val initialBlocks = note.content.split("\n\n").map { 
                NoteBlock(
                    id = EditorLogic.generateId(), 
                    content = it, 
                    detectedAttributes = EditorLogic.extractAttributes(it),
                    astNode = editor.ast.AstParser.parseBlock(EditorLogic.generateId(), it)
                ) 
            }
            val blocks = initialBlocks.ifEmpty { listOf(EditorLogic.createInitialBlock()) }
            
            _state.update { state ->
                state.copy(
                    noteId = noteId,
                    navigationStack = EditorLogic.updateNavigationStack(state.navigationStack, noteId),
                    blocks = blocks,
                    focusedBlockId = blocks.firstOrNull()?.id
                )
            }
        }
    }



    private fun updateBlockContent(blockId: String, newContent: String) {
        // 1. Local State (instantâneo): O estado armazena exatamente o texto puro digitado sem bloquear a UI.
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block.id == blockId) {
                    block.copy(
                        content = newContent,
                        detectedAttributes = EditorLogic.extractAttributes(newContent)
                    )
                } else {
                    block
                }
            }
            state.copy(blocks = newBlocks)
        }
        
        // 2. Envia para o fluxo de parsing em background
        textChangeFlow.tryEmit(blockId to newContent)
    }

    private suspend fun parseBlockBackground(blockId: String, content: String) {
        // 3. Parser (Background Thread): AST analisa o bloco
        val astNode = editor.ast.AstParser.parseBlock(blockId, content)

        // 4. UI Update: O componente muda para refletir a nova AST de forma atômica
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block.id == blockId) {
                    block.copy(astNode = astNode)
                } else {
                    block
                }
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun mutateBlocks(action: BlockAction) {
        _state.update { state ->
            EditorLogic.mutateBlocks(state, action)
        }
    }

    private fun launchCreateNote() {
        coroutineScope.launch {
            val newId = EditorLogic.generateId()
            val initialBlock = EditorLogic.createInitialBlock()
            val newNote = Note(
                id = newId,
                title = "Untitled",
                content = "",
                attributes = emptyList(),
                connections = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(newNote)
            
            _state.update { 
                it.copy(
                    noteId = newId,
                    navigationStack = EditorLogic.updateNavigationStack(it.navigationStack, newId),
                    blocks = listOf(initialBlock),
                    focusedBlockId = initialBlock.id,
                    showResonanceFilter = false,
                    originalThought = ""
                ) 
            }
        }
    }

}
