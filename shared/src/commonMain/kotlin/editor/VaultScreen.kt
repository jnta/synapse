package editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.synapse.domain.model.Note
import dev.synapse.domain.model.NoteMetadata
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun VaultScreen(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Vault",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = SynapseColors.Primary
            )
        )
        Text(
            text = "${state.noteSummaries.size} NOTES IN REPOSITORY",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = state.noteSummaries,
                key = { note: NoteMetadata -> note.id }
            ) { note: NoteMetadata ->
                NoteGridItem(note = note, onClick = { onEvent(EditorUiEvent.SelectNote(note.id)) })
            }
        }
    }
}

@Composable
private fun NoteGridItem(
    note: NoteMetadata,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        elevation = 0.dp,
        backgroundColor = SynapseColors.SurfaceContainerLowest,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SynapseColors.Primary
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.snippet,
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    maxLines = 3
                )
            }
            
            Text(
                text = formatTimestamp(note.updatedAt).uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SynapseColors.OnSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    }
}
