package editor

import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata
import kotlinx.datetime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteEditorScreen(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(SynapseColors.Surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorTopBar()
            BreadcrumbTrail(
                navigationStack = state.navigationStack,
                activeNoteId = state.noteId,
                noteSummaries = state.noteSummaries,
                onEvent = onEvent
            )
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                EditorContentColumn(state, onEvent)
            }
        }
    }
}

@Composable
private fun EditorContentColumn(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = SynapseDimensions.MaxEditorWidth)
            .fillMaxHeight()
            .padding(top = 64.dp)
            .padding(horizontal = SynapseDimensions.EditorHorizontalPadding)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 100.dp),
                color = MaterialTheme.colors.primary
            )
        } else if (state.noteId.isEmpty()) {
            EmptyEditorState(onEvent = onEvent)
        } else {
            val currentNote = state.notes.find { it.id == state.noteId }
            if (currentNote != null) {
                NoteHeader(note = currentNote)
                Spacer(modifier = Modifier.height(24.dp))
            }
            BlockEditorArea(
                blocks = state.blocks,
                focusedBlockId = state.focusedBlockId,
                shouldMask = (currentNote?.viewCount ?: 0) >= 2,
                noteSummaries = state.noteSummaries,
                currentNoteId = state.noteId,
                onEvent = onEvent,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

@Composable
fun BlockEditorArea(
    blocks: List<NoteBlock>,
    focusedBlockId: String?,
    shouldMask: Boolean,
    noteSummaries: List<NoteMetadata>,
    currentNoteId: String,
    onEvent: (EditorUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(focusedBlockId) {
        focusedBlockId?.let { id ->
            focusRequesters[id]?.requestFocus()
        }
    }

    Box(modifier = modifier.padding(16.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(blocks, key = { block -> block.id }) { block ->
                val focusRequester = remember { FocusRequester() }
                focusRequesters[block.id] = focusRequester

                BlockItem(
                    block = block,
                    isFocused = focusedBlockId == block.id,
                    shouldMask = shouldMask,
                    noteSummaries = noteSummaries,
                    currentNoteId = currentNoteId,
                    focusRequester = focusRequester,
                    onEvent = onEvent
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BlockItem(
    block: NoteBlock,
    isFocused: Boolean,
    shouldMask: Boolean,
    noteSummaries: List<NoteMetadata>,
    currentNoteId: String,
    focusRequester: FocusRequester,
    onEvent: (EditorUiEvent) -> Unit
) {
    var showSlashMenu by remember { mutableStateOf(false) }
    var textValue by remember(block.id) { 
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(block.content)) 
    }

    LaunchedEffect(block.content) {
        if (textValue.text != block.content) {
            textValue = textValue.copy(
                text = block.content,
                selection = androidx.compose.ui.text.TextRange(block.content.length)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.Transparent),
        verticalAlignment = Alignment.Top
    ) {
        BlockPrefix(block)

        // Drag Handle (Visual Placeholder)
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Reorder",
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(top = 4.dp, end = 8.dp, start = 8.dp)
                .size(20.dp)
        )

        Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
            var slashQuery by remember { mutableStateOf("") }
            
            LaunchedEffect(textValue.text) {
                onEvent(EditorUiEvent.UpdateBlockContent(block.id, textValue.text))
                
                val slashIndex = textValue.text.lastIndexOf('/')
                if (slashIndex != -1) {
                    val afterSlash = textValue.text.substring(slashIndex + 1)
                    if (!afterSlash.contains(' ')) {
                        slashQuery = afterSlash
                        showSlashMenu = true
                    } else {
                        showSlashMenu = false
                    }
                } else {
                    showSlashMenu = false
                }
            }
            
            BlockTextField(
                textValue = textValue,
                onValueChange = { textValue = it },
                block = block,
                isFocused = isFocused,
                shouldMask = shouldMask,
                focusRequester = focusRequester,
                onEvent = onEvent
            )

            SlashCommandMenu(
                expanded = showSlashMenu,
                onDismiss = { showSlashMenu = false },
                onSelect = { prefix ->
                    onEvent(EditorUiEvent.UpdateBlockContent(block.id, prefix))
                    showSlashMenu = false
                },
                noteSummaries = noteSummaries,
                currentNoteId = currentNoteId,
                query = slashQuery,
                onLinkSelect = { targetNote ->
                    val contentBeforeSlash = block.content.substringBeforeLast("/")
                    val newContent = contentBeforeSlash + "[[" + targetNote.title + "]]"
                    onEvent(EditorUiEvent.UpdateBlockContent(block.id, newContent))
                    showSlashMenu = false
                }
            )
        }
    }
}
