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

@Composable
fun ContextPanel(
    resonanceItems: List<ResonanceItem>
) {
    Column(
        modifier = Modifier
            .width(SynapseDimensions.ContextPanelWidth)
            .fillMaxHeight()
            .background(SynapseColors.Panel)
            .padding(16.dp)
    ) {
        Text(
            text = "RESONANCE",
            style = MaterialTheme.typography.overline,
            color = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (resonanceItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Press Ctrl+I to resonate",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(resonanceItems) { item ->
                    ResonanceCard(item)
                }
            }
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
