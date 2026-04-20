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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
            text = "Graph Visualization".uppercase(),
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
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
                text = "Forward Links".uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.Gray.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(state.forwardLinks) { note ->
                    LinkCard(note, onEvent)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Back Links
        if (state.backLinks.isNotEmpty()) {
            Text(
                text = "Back Links".uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.Gray.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(state.backLinks) { note ->
                    LinkCard(note, onEvent)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "Resonance".uppercase(),
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.Gray.copy(alpha = 0.5f)
            ),
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

private const val GRAPH_RADIUS = 80f
private const val NODE_RADIUS = 4f
private const val CENTER_RADIUS = 20f
private const val STROKE_WIDTH = 1f

@Composable
fun GraphFocusWidget(
    forwardLinks: List<Note>,
    backLinks: List<Note>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(color = Color.White.copy(alpha = 0.2f), radius = CENTER_RADIUS, center = center)
            
            forwardLinks.forEachIndexed { i, _ ->
                val angle = (i.toFloat() / forwardLinks.size) * kotlin.math.PI
                val x = center.x + GRAPH_RADIUS * kotlin.math.cos(angle).toFloat()
                val y = center.y - GRAPH_RADIUS * kotlin.math.sin(angle).toFloat()
                drawLine(
                    color = SynapseColors.Primary.copy(alpha = 0.2f),
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = STROKE_WIDTH
                )
                drawCircle(color = SynapseColors.Primary, radius = NODE_RADIUS, center = Offset(x, y))
            }
            
            backLinks.forEachIndexed { i, _ ->
                val angle = (i.toFloat() / backLinks.size) * kotlin.math.PI
                val x = center.x - GRAPH_RADIUS * kotlin.math.cos(angle).toFloat()
                val y = center.y + GRAPH_RADIUS * kotlin.math.sin(angle).toFloat()
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = STROKE_WIDTH
                )
                drawCircle(color = Color.Gray, radius = NODE_RADIUS, center = Offset(x, y))
            }
        }
        Text(
            text = "Active Context",
            style = MaterialTheme.typography.caption.copy(fontSize = 9.sp),
            color = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun LinkCard(note: Note, onEvent: (EditorUiEvent) -> Unit) {
    Card(
        backgroundColor = SynapseColors.SurfaceContainerLowest,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth().clickable { onEvent(EditorUiEvent.SelectNote(note.id)) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                color = SynapseColors.OnSurface
            )
            
            // Meta-Pills
            if (note.attributes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    note.attributes.take(3).forEach { attr ->
                        val pillText = if (attr.key == "tag") {
                            "#${attr.value.uppercase()}"
                        } else {
                            "${attr.key}:${attr.value}"
                        }
                        Text(
                            text = pillText,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
                            color = SynapseColors.OnSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    SynapseColors.SurfaceContainerHigh,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResonanceCard(item: ResonanceItem) {
    Card(
        backgroundColor = SynapseColors.SurfaceContainerLowest,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                color = SynapseColors.OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.snippet,
                style = MaterialTheme.typography.caption,
                color = Color.Gray.copy(alpha = 0.8f),
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
