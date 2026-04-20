package editor

import dev.synapse.domain.model.Note
import kotlinx.datetime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val state by viewModel.state.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(SynapseColors.Background) // Sleek dark mode background
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                    when (event.key) {
                        Key.N -> {
                            viewModel.onEvent(EditorUiEvent.CreateNewNote)
                            true
                        }
                        Key.S -> {
                            viewModel.onEvent(EditorUiEvent.SaveCurrentNote)
                            true
                        }
                        Key.I -> {
                            viewModel.onEvent(EditorUiEvent.Resonate)
                            true
                        }
                        Key.Enter -> {
                            state.focusedBlockId?.let { 
                                viewModel.onEvent(EditorUiEvent.NoteOverflow(it))
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // LeftNav: Fixed Sidebar
        AnimatedVisibility(visible = state.isSidebarVisible) {
            LeftNav(
                notes = state.notes,
                selectedNoteId = state.noteId,
                onEvent = viewModel::onEvent
            )
        }

        // EditorViewport: Center Fluid Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(SynapseColors.Surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BreadcrumbTrail(
                    navigationStack = state.navigationStack,
                    activeNoteId = state.noteId,
                    notes = state.notes,
                    onEvent = viewModel::onEvent
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = SynapseDimensions.MaxEditorWidth) // Max-width 800px constraint
                            .fillMaxHeight()
                            .padding(horizontal = SynapseDimensions.EditorHorizontalPadding)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 100.dp),
                                color = MaterialTheme.colors.primary
                            )
                        } else if (state.noteId.isEmpty()) {
                            EmptyEditorState(onEvent = viewModel::onEvent)
                        } else {
                            val currentNote = state.notes.find { it.id == state.noteId }
                            
                            if (currentNote != null) {
                                NoteHeader(
                                    note = currentNote,
                                    forwardLinks = state.forwardLinks,
                                    backLinks = state.backLinks,
                                    onNoteClick = { id -> viewModel.onEvent(EditorUiEvent.SelectNote(id)) }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            BlockEditorArea(
                                blocks = state.blocks,
                                focusedBlockId = state.focusedBlockId,
                                shouldMask = (currentNote?.viewCount ?: 0) >= 2,
                                notes = state.notes,
                                currentNoteId = state.noteId,
                                onEvent = viewModel::onEvent,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // ContextPanel: Right Sidebar
        AnimatedVisibility(visible = state.isContextPanelVisible) {
            ContextPanel(
                state = state,
                onEvent = viewModel::onEvent
            )
        }
    }

    // Resonance Filter Modal (existing logic preserved)
    if (state.showResonanceFilter) {
        ResonanceFilterModal(state, viewModel)
    }
}

@Composable
fun LeftNav(
    notes: List<Note>,
    selectedNoteId: String?,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(SynapseDimensions.LeftNavWidth)
            .fillMaxHeight()
            .background(SynapseColors.Panel)
            .padding(16.dp)
    ) {
        Text(
            text = "SYNAPSE",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = SynapseTypography.BrandFontSize,
                letterSpacing = SynapseTypography.BrandLetterSpacing,
                color = Color.Gray.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(bottom = 32.dp, start = 8.dp)
        )

        PerspectiveList()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "VAULT",
            style = MaterialTheme.typography.overline,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes, key = { it.id }) { note ->
                val onClick = remember(note.id) { { onEvent(EditorUiEvent.SelectNote(note.id)) } }
                NoteListItem(
                    note = note,
                    isSelected = note.id == selectedNoteId,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun PerspectiveList() {
    val perspectives = listOf(
        "#Captura" to SynapseColors.PerspectiveCapture,
        "#Referência" to SynapseColors.PerspectiveReference,
        "#Síntese" to SynapseColors.PerspectiveSynthesis,
        "#Mapa" to SynapseColors.PerspectiveMap
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        perspectives.forEach { (name, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.body2,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun BreadcrumbTrail(
    navigationStack: List<String>,
    activeNoteId: String,
    notes: List<Note>,
    onEvent: (EditorUiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SynapseDimensions.BreadcrumbHorizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigationStack.forEachIndexed { index, noteId ->
            val noteTitle = notes.find { it.id == noteId }?.title ?: "Untitled"
            
            Text(
                text = noteTitle,
                style = MaterialTheme.typography.caption,
                color = if (noteId == activeNoteId) Color.White else Color.Gray,
                modifier = Modifier.clickable { onEvent(EditorUiEvent.SelectNote(noteId)) }
            )
            
            if (index < navigationStack.lastIndex) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}



@Composable
fun EmptyEditorState(onEvent: (EditorUiEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select or create a note to begin", 
            style = MaterialTheme.typography.h6,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onEvent(EditorUiEvent.CreateNewNote) },
            colors = ButtonDefaults.buttonColors(backgroundColor = SynapseColors.Primary, contentColor = Color.White)
        ) {
            Text("Create New Note")
        }
    }
}

@Composable
fun ResonanceFilterModal(state: EditorUiState, viewModel: EditorViewModel) {
    AlertDialog(
        onDismissRequest = { /* Force interaction */ },
        backgroundColor = SynapseColors.Selection,
        contentColor = Color.White,
        title = { Text("The Resonance Filter", style = MaterialTheme.typography.h6) },
        text = {
            Column {
                Text(
                    "Silent saves are blocked. Friction as a feature.",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Synthesis required: Add at least ${state.minThoughtLength} " +
                    "characters of 'Original Thought' to commit this note.",
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.originalThought,
                    onValueChange = { 
                        viewModel.onEvent(EditorUiEvent.UpdateOriginalThought(it)) 
                    },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("Synthesis/Reflection...", color = Color.Gray) },
                    label = { Text("Original Thought") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Text(
                    "${state.originalThought.length} / ${state.minThoughtLength}",
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.caption,
                    color = if (state.originalThought.length >= state.minThoughtLength) {
                        SynapseColors.Success 
                    } else {
                        SynapseColors.Error
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onEvent(EditorUiEvent.CommitNote) },
                enabled = state.originalThought.length >= state.minThoughtLength,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (state.originalThought.length >= state.minThoughtLength) {
                        SynapseColors.Primary 
                    } else {
                        Color.Gray
                    },
                    contentColor = Color.White
                )
            ) {
                Text("Commit to Vault")
            }
        }
    )
}

@Composable
fun NoteListItem(
    note: Note, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) SynapseColors.Selection else Color.Transparent
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = note.title.ifEmpty { "Untitled" },
            style = MaterialTheme.typography.subtitle2,
            color = if (isSelected) Color.White else Color.Gray,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (note.snippet.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.snippet,
                style = MaterialTheme.typography.caption,
                color = Color.Gray.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}




@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BlockEditorArea(
    blocks: List<NoteBlock>,
    focusedBlockId: String?,
    shouldMask: Boolean,
    notes: List<Note>,
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
                    notes = notes,
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
    notes: List<Note>,
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
                notes = notes,
                currentNoteId = currentNoteId,
                query = slashQuery,
                onLinkSelect = { targetNote ->
                    val contentBeforeSlash = block.content.substringBeforeLast("/")
                    onEvent(EditorUiEvent.UpdateBlockContent(block.id, contentBeforeSlash + "[[" + targetNote.title + "]]"))
                    showSlashMenu = false
                }
            )
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteHeader(
    note: Note,
    forwardLinks: List<Note>,
    backLinks: List<Note>,
    onNoteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(Color.White.copy(alpha = 0.03f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        // Title
        Text(
            text = note.title.uppercase(),
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colors.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // Metadata Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Created",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatTimestamp(note.createdAt),
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Tags
            note.attributes.filter { it.key == "tag" }.forEach { tag ->
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${tag.value}",
                        style = MaterialTheme.typography.overline,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }

        val allLinks = (forwardLinks + backLinks).distinctBy { it.id }
        if (allLinks.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.1f))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("NETWORKED CONTEXT", style = MaterialTheme.typography.overline, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allLinks.forEach { link ->
                        Text(
                            text = link.title,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.05f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .clickable { onNoteClick(link.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${dateTime.dayOfMonth}, ${dateTime.year}"
}
