package editor
 
import dev.synapse.domain.model.Note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlockPrefix(block: NoteBlock) {
    if (block.astNode is editor.ast.NoteNode.BlockNode.ListItem) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp, start = 8.dp, end = 4.dp)
                .size(6.dp)
                .background(
                    MaterialTheme.colors.onBackground, 
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BlockTextField(
    textValue: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    block: NoteBlock,
    isFocused: Boolean,
    shouldMask: Boolean,
    focusRequester: FocusRequester,
    onEvent: (EditorUiEvent) -> Unit
) {
    BasicTextField(
        value = textValue,
        onValueChange = onValueChange,
        visualTransformation = MarkdownVisualTransformation(shouldMask = shouldMask),
        textStyle = getBlockTextStyle(block.astNode),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                if (state.isFocused && !isFocused) {
                    onEvent(EditorUiEvent.FocusBlock(block.id))
                }
            }
            .onPreviewKeyEvent { event ->
                handleKeyEvent(event, textValue, block.id, onEvent)
            },
        decorationBox = { innerTextField ->
            if (textValue.text.isEmpty() && isFocused) {
                Text(
                    text = "Type '/' for commands",
                    color = Color.Gray.copy(alpha = 0.5f)
                )
            }
            innerTextField()
        }
    )
}

@Composable
fun getBlockTextStyle(node: editor.ast.NoteNode.BlockNode): TextStyle {
    return when (node) {
        is editor.ast.NoteNode.BlockNode.Heading -> {
            val fontSize = when (node.level) {
                1 -> 28.sp
                2 -> 24.sp
                else -> 20.sp
            }
            TextStyle(
                color = MaterialTheme.colors.onBackground,
                fontSize = fontSize,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                lineHeight = fontSize * 1.3
            )
        }
        else -> TextStyle(
            color = MaterialTheme.colors.onBackground,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun handleKeyEvent(
    event: KeyEvent,
    textValue: androidx.compose.ui.text.input.TextFieldValue,
    blockId: String,
    onEvent: (EditorUiEvent) -> Unit
): Boolean {
    if (event.type == KeyEventType.KeyDown) {
        return when (event.key) {
            Key.Enter -> {
                onEvent(EditorUiEvent.AddBlockAfter(blockId))
                true
            }
            Key.Backspace -> {
                if (textValue.text.isEmpty()) {
                    onEvent(EditorUiEvent.RemoveBlock(blockId))
                    true
                } else false
            }
            else -> false
        }
    }
    return false
}

@Composable
fun SlashCommandMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    notes: List<Note> = emptyList(),
    currentNoteId: String = "",
    query: String = "",
    onLinkSelect: (Note) -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.PopupProperties(focusable = false)
    ) {
        SlashMenuContent(
            notes = notes,
            currentNoteId = currentNoteId,
            query = query,
            onSelect = onSelect,
            onLinkSelect = onLinkSelect,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun SlashMenuContent(
    notes: List<Note>,
    currentNoteId: String,
    query: String,
    onSelect: (String) -> Unit,
    onLinkSelect: (Note) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredNotes = notes.filter { 
        it.id != currentNoteId && (query.isEmpty() || it.title.contains(query, ignoreCase = true)) 
    }.take(10)

    if (query.isEmpty()) {
        DropdownMenuItem(onClick = { onSelect("# ") }) {
            Text("Heading 1")
        }
        DropdownMenuItem(onClick = { onSelect("## ") }) {
            Text("Heading 2")
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))
    }

    if (filteredNotes.isNotEmpty()) {
        Text(
            "Link to Note", 
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.overline,
            color = Color.Gray
        )
    }

    for (note in filteredNotes) {
        DropdownMenuItem(onClick = { 
            onLinkSelect(note)
            onDismiss()
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colors.primary, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(note.title, style = MaterialTheme.typography.body2)
                    Text(note.id.take(8), style = MaterialTheme.typography.overline, color = Color.Gray)
                }
            }
        }
    }
}
