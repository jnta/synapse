package editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import dev.synapse.domain.model.Note
import editor.EditorUiState
import editor.EditorUiEvent
import editor.ResonanceItem
import editor.SynapseColors
import editor.SynapseDimensions

@Composable
fun ContextPanel(
    state: EditorUiState,
    onEvent: (EditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(SynapseDimensions.ContextPanelWidth)
            .fillMaxHeight()
            .background(SynapseColors.Panel)
            .padding(16.dp)
    ) {
        // Graph Visualization Placeholder
        Text(
            text = "GRAPH",
            style = MaterialTheme.typography.overline,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        GraphFocusWidget(
            forwardLinks = state.forwardLinks,
            backLinks = state.backLinks,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(SynapseColors.Surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Forward Links
        if (state.forwardLinks.isNotEmpty()) {
            Text(
                text = "FORWARD LINKS",
                style = MaterialTheme.typography.overline,
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(state.forwardLinks) { note ->
                    LinkCard(note, onEvent)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Back Links
        if (state.backLinks.isNotEmpty()) {
            Text(
                text = "BACK LINKS",
                style = MaterialTheme.typography.overline,
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(state.backLinks) { note ->
                    LinkCard(note, onEvent)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "RESONANCE",
            style = MaterialTheme.typography.overline,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (state.resonanceItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Press Ctrl+I to resonate",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(state.resonanceItems) { item ->
                    ResonanceCard(item)
                }
            }
        }
    }
}

@Composable
fun GraphFocusWidget(
    forwardLinks: List<Note>,
    backLinks: List<Note>,
    modifier: Modifier = Modifier
) {
    // Placeholder for Phase 4 Canvas visualization
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw a simple central node and orbiting nodes?
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 20f, center = center)
            
            // Draw forward links
            forwardLinks.forEachIndexed { i, _ ->
                val angle = (i.toFloat() / forwardLinks.size) * kotlin.math.PI
                val x = center.x + 80 * kotlin.math.cos(angle).toFloat()
                val y = center.y - 80 * kotlin.math.sin(angle).toFloat()
                drawLine(color = SynapseColors.Primary.copy(alpha = 0.5f), start = center, end = Offset(x, y), strokeWidth = 2f)
                drawCircle(color = SynapseColors.Primary, radius = 6f, center = Offset(x, y))
            }
            
            // Draw back links
            backLinks.forEachIndexed { i, _ ->
                val angle = (i.toFloat() / backLinks.size) * kotlin.math.PI
                val x = center.x - 80 * kotlin.math.cos(angle).toFloat()
                val y = center.y + 80 * kotlin.math.sin(angle).toFloat()
                drawLine(color = SynapseColors.PerspectiveReference.copy(alpha = 0.5f), start = center, end = Offset(x, y), strokeWidth = 2f)
                drawCircle(color = SynapseColors.PerspectiveReference, radius = 6f, center = Offset(x, y))
            }
        }
        Text("Graph", style = MaterialTheme.typography.caption, color = Color.Gray)
    }
}

@Composable
fun LinkCard(note: Note, onEvent: (EditorUiEvent) -> Unit) {
    Card(
        backgroundColor = SynapseColors.Surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onEvent(EditorUiEvent.SelectNote(note.id)) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.subtitle2,
                color = Color.White
            )
        }
    }
}

@Composable
fun ResonanceCard(item: ResonanceItem) {
    Card(
        backgroundColor = SynapseColors.Surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.subtitle2,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.snippet,
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.tags.forEach { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.overline,
                            color = SynapseColors.Primary,
                            modifier = Modifier
                                .background(
                                    SynapseColors.Primary.copy(alpha = 0.1f), 
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
