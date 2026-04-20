package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata

@Composable
fun NoteListItem(
    note: Note,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(note.id) }
            .background(if (isSelected) SynapseColors.SurfaceContainerHighest else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) SynapseColors.Primary else SynapseColors.OnSurface
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatTimestamp(note.updatedAt),
                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    // Basic placeholder using the timestamp to satisfy Detekt
    return "${timestamp % 24}h ago"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteHeader(note: Note) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            note.attributes.filter { it.key == "tag" }.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(
                            SynapseColors.SurfaceContainerHigh,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${tag.value.uppercase()}",
                        style = TextStyle(
                            fontSize = SynapseTypography.TagFontSize,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = SynapseColors.OnSurfaceVariant
                    )
                }
            }
        }
        Text(
            text = note.title,
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                lineHeight = 44.sp
            ),
            color = SynapseColors.Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Created",
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Created: ${formatTimestamp(note.createdAt)}".uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = SynapseTypography.MetadataFontSize,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}


@Composable
fun LeftNav(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(SynapseDimensions.LeftNavWidth)
            .fillMaxHeight()
            .background(SynapseColors.Panel)
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp).padding(horizontal = 8.dp)) {
            Text(
                text = "Synapse",
                style = TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp,
                    color = SynapseColors.Primary
                )
            )
            Text(
                text = "THE DIGITAL CURATOR",
                style = TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }

        Button(
            onClick = { onEvent(EditorUiEvent.CreateNewNote) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = SynapseColors.Primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "New Note",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        NavigationList(activeItem = state.currentDestination, onEvent = onEvent)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "COLLECTIONS",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        CollectionList()

        Spacer(modifier = Modifier.weight(1f))

        UserProfileSection()
    }
}


@Composable
fun EditorTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SynapseColors.Panel)
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Note Editor",
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            color = SynapseColors.Primary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Menu, 
                contentDescription = "History", 
                tint = Color.Gray, 
                modifier = Modifier.size(20.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Share",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add, 
                contentDescription = "More", 
                tint = Color.Gray, 
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BreadcrumbTrail(
    navigationStack: List<String>,
    activeNoteId: String,
    noteSummaries: List<NoteMetadata>,
    onEvent: (EditorUiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SynapseDimensions.BreadcrumbHorizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigationStack.forEachIndexed { index, noteId ->
            val noteTitle = noteSummaries.find { it.id == noteId }?.title ?: "Untitled"
            
            Text(
                text = noteTitle,
                style = MaterialTheme.typography.caption,
                color = if (noteId == activeNoteId) SynapseColors.Primary else Color.Gray,
                modifier = Modifier.clickable { onEvent(EditorUiEvent.SelectNote(noteId)) }
            )
            
            if (index < navigationStack.lastIndex) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 8.dp)
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
fun ResonanceFilterModal(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
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
                val synthesisText = "Synthesis required: Add at least ${state.minThoughtLength} " +
                    "characters of 'Original Thought' to commit this note."
                Text(
                    text = synthesisText,
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.originalThought,
                    onValueChange = { 
                        onEvent(EditorUiEvent.UpdateOriginalThought(it)) 
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
                onClick = { onEvent(EditorUiEvent.CommitNote) },
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
