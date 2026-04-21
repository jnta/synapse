package editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlin.random.Random
import dev.synapse.domain.repository.NoteRepository
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata
import dev.synapse.domain.util.NoteParser
import dev.synapse.domain.repository.ResonanceRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

private const val DEBOUNCE_MS = 300L
private const val AUTO_SAVE_DEBOUNCE_MS = 2000L
private const val MAX_TITLE_LENGTH = 50

@OptIn(FlowPreview::class)
class EditorViewModel(
    private val coroutineScope: CoroutineScope,
    private val repository: NoteRepository,
    private val resonanceRepository: ResonanceRepository
) {


    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val textChangeFlow = MutableSharedFlow<Pair<String, String>>(
        extraBufferCapacity = 64
    )

    init {
        onEvent(EditorUiEvent.LoadNotes)
        
        coroutineScope.launch {
            repository.getCollections().collect { collections ->
                _state.update { it.copy(collections = collections) }
            }
        }

        coroutineScope.launch {
            combine(
                repository.getNoteSummaries(),
                _state.map { it.selectedCollectionIds }.distinctUntilChanged()
            ) { summaries, collections ->
                if (collections.isEmpty()) summaries
                else summaries.filter { it.collectionId in collections }
            }.collect { filteredSummaries ->
                _state.update { it.copy(noteSummaries = filteredSummaries, isLoading = false) }
            }
        }

        coroutineScope.launch {
            combine(
                repository.getAllNotes(),
                _state.map { it.selectedCollectionIds }.distinctUntilChanged()
            ) { notes, collections ->
                if (collections.isEmpty()) notes
                else notes.filter { it.collectionId in collections }
            }.collect { filteredNotes ->
                _state.update { it.copy(notes = filteredNotes) }
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

        coroutineScope.launch {
            _state.map { it.noteId }.distinctUntilChanged().collectLatest { noteId ->
                if (noteId.isNotEmpty()) {
                    launch {
                        repository.getForwardLinks(noteId).collect { links ->
                            _state.update { it.copy(forwardLinks = links) }
                        }
                    }
                    launch {
                        repository.getBackLinks(noteId).collect { links ->
                            _state.update { it.copy(backLinks = links) }
                        }
                    }
                } else {
                    _state.update { it.copy(forwardLinks = emptyList(), backLinks = emptyList()) }
                }
            }
        }
    }

    private val operations = EditorOperations(coroutineScope, repository, resonanceRepository, _state)

    fun onEvent(event: EditorUiEvent) {
        if (!handleNoteEvent(event)) {
            when (event) {
                is EditorUiEvent.UpdateBlockContent -> updateBlockContent(event.blockId, event.newContent)
                is EditorUiEvent.AddBlockAfter -> _state.update { 
                    EditorLogic.mutateBlocks(it, BlockAction.Add(event.blockId)) 
                }
                is EditorUiEvent.RemoveBlock -> _state.update { 
                    EditorLogic.mutateBlocks(it, BlockAction.Remove(event.blockId)) 
                }
                is EditorUiEvent.MoveBlock -> _state.update { 
                    EditorLogic.mutateBlocks(it, BlockAction.Move(event.fromIndex, event.toIndex)) 
                }
                is EditorUiEvent.FocusBlock -> _state.update { 
                    it.copy(focusedBlockId = event.blockId) 
                }
                else -> {}
            }
        }
    }

    private fun handleNoteEvent(event: EditorUiEvent): Boolean {
        return when (event) {
            is EditorUiEvent.SelectNote,
            is EditorUiEvent.MapsTo -> handleSelectionEvent(event)
            is EditorUiEvent.UpdateNoteTitle -> handleUpdateNoteTitle(event)
            is EditorUiEvent.Resonate -> { operations.resonate(); true }
            is EditorUiEvent.NoteOverflow -> { 
                operations.handleNoteOverflow(event.blockId) { selectNote(it) }
                true 
            }
            is EditorUiEvent.CreateNewNote -> { operations.createNewNote(); true }
            is EditorUiEvent.SaveCurrentNote,
            is EditorUiEvent.CommitNote,
            is EditorUiEvent.UpdateNoteCollection -> handlePersistenceEvents(event)
            is EditorUiEvent.UpdateOriginalThought -> { 
                _state.update { it.copy(originalThought = event.text) }; true 
            }
            is EditorUiEvent.ToggleSidebar,
            is EditorUiEvent.ToggleContextPanel,
            is EditorUiEvent.ToggleCollectionFilter -> handleToggleEvents(event)
            is EditorUiEvent.ShowCreateCollectionDialog -> {
                _state.update { it.copy(showCreateCollectionDialog = true, editingCollection = null) }
                true
            }
            is EditorUiEvent.DismissCollectionDialog -> {
                _state.update { it.copy(showCreateCollectionDialog = false, editingCollection = null, collectionError = null) }
                true
            }
            is EditorUiEvent.EditCollection -> {
                _state.update { it.copy(showCreateCollectionDialog = true, editingCollection = event.collection) }
                true
            }
            is EditorUiEvent.SaveCollection,
            is EditorUiEvent.DeleteCollection -> handleCollectionCRUD(event)
            is EditorUiEvent.LinkNotes -> { operations.linkNotes(event); true }
            is EditorUiEvent.NavigateTo -> {
                _state.update { it.copy(currentDestination = event.destination) }
                true
            }
            is EditorUiEvent.DeleteNote -> { operations.deleteNote(event.noteId); true }
            else -> false
        }
    }

    private fun handlePersistenceEvents(event: EditorUiEvent): Boolean {
        when (event) {
            is EditorUiEvent.SaveCurrentNote -> handleSave(isCommit = false)
            is EditorUiEvent.CommitNote -> handleSave(isCommit = true)
            is EditorUiEvent.UpdateNoteCollection -> 
                operations.updateNoteCollection(event.collectionId) { handleSave(isCommit = false) }
            else -> return false
        }
        return true
    }

    private fun handleSelectionEvent(event: EditorUiEvent): Boolean {
        val noteId = when (event) {
            is EditorUiEvent.SelectNote -> event.noteId
            is EditorUiEvent.MapsTo -> event.noteId
            else -> return false
        }
        selectNote(noteId)
        return true
    }

    private fun handleToggleEvents(event: EditorUiEvent): Boolean {
        when (event) {
            is EditorUiEvent.ToggleSidebar -> 
                _state.update { it.copy(isSidebarVisible = !it.isSidebarVisible) }
            is EditorUiEvent.ToggleContextPanel -> 
                _state.update { it.copy(isContextPanelVisible = !it.isContextPanelVisible) }
            is EditorUiEvent.ToggleCollectionFilter -> {
                if (_state.value.currentDestination != "Editor") {
                    _state.update { state ->
                        val newSelected = if (state.selectedCollectionIds.contains(event.collectionId)) {
                            state.selectedCollectionIds - event.collectionId
                        } else {
                            state.selectedCollectionIds + event.collectionId
                        }
                        state.copy(selectedCollectionIds = newSelected)
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun handleCollectionCRUD(event: EditorUiEvent): Boolean {
        coroutineScope.launch {
            when (event) {
                is EditorUiEvent.SaveCollection -> {
                    val id = if (event.id.isEmpty()) {
                        event.name.lowercase().replace(" ", "-") + "-" + Random.nextInt(1000)
                    } else {
                        event.id
                    }
                    repository.saveCollection(dev.synapse.domain.model.NoteCollection(id, event.name, event.color))
                    _state.update { it.copy(showCreateCollectionDialog = false, editingCollection = null) }
                }
                is EditorUiEvent.DeleteCollection -> {
                    if (repository.isCollectionEmpty(event.id)) {
                        repository.deleteCollection(event.id)
                        _state.update { it.copy(showCreateCollectionDialog = false, editingCollection = null, collectionError = null) }
                    } else {
                        _state.update { it.copy(collectionError = "Cannot delete collection that has notes associated to it.") }
                    }
                }
                else -> {}
            }
        }
        return true
    }

    private fun handleUpdateNoteTitle(event: EditorUiEvent.UpdateNoteTitle): Boolean {
        _state.update { state ->
            val updatedNotes = state.notes.map { note ->
                if (note.id == state.noteId) note.copy(title = event.title) else note
            }
            state.copy(notes = updatedNotes)
        }
        return true
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
        
        coroutineScope.launch {
            val existingNote = repository.getNoteById(selectedId)
            
            // Prioritize the title from the state (which might have been manually renamed)
            val currentTitle = _state.value.notes.find { it.id == selectedId }?.title
            val derivedTitle = currentTitle?.ifEmpty { null } ?: _state.value.blocks.firstOrNull()?.content
                ?.lineSequence()?.firstOrNull()?.removePrefix("# ")?.take(MAX_TITLE_LENGTH) ?: "Untitled"
            val extractedEdges = NoteParser.extractEdges(selectedId, fullContent)
            
            // Resolve Titles to IDs
            val resolvedEdges = extractedEdges.map { edge ->
                val targetNote = repository.getNoteByTitle(edge.targetId)
                if (targetNote != null) {
                    edge.copy(targetId = targetNote.id)
                } else {
                    edge // Keep title if not found, or maybe filter out? 
                    // Usually in these systems we keep it as a "broken link" or title reference
                }
            }

            val currentCollectionId = _state.value.notes.find { it.id == selectedId }?.collectionId 
                ?: "raw"

            val updatedNote = Note(
                id = selectedId,
                title = derivedTitle,
                content = fullContent,
                collectionId = currentCollectionId,
                attributes = NoteParser.extractAttributes(fullContent),
                connections = resolvedEdges,
                createdAt = existingNote?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                embedding = existingNote?.embedding // Keep existing for now, separate job updates it
            )
            repository.saveNote(updatedNote)
            
            // Trigger embedding update in background
            coroutineScope.launch {
                resonanceRepository.updateEmbedding(selectedId, fullContent)
            }
            
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
                    currentDestination = "Editor",
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


}
