import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color
import editor.SynapseColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import editor.EditorScreen
import editor.EditorViewModel

import dev.synapse.database.DatabaseDriverFactory
import dev.synapse.database.SynapseDatabase
import dev.synapse.data.repository.NoteRepositoryImpl

@Composable
fun App() {
    val darkColors = darkColors(
        primary = SynapseColors.Primary,
        background = SynapseColors.Background,
        surface = SynapseColors.Surface,
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(colors = darkColors) {
        val coroutineScope = rememberCoroutineScope()
        val database = remember { 
            SynapseDatabase(DatabaseDriverFactory().createDriver()) 
        }
        val noteRepository = remember { NoteRepositoryImpl(database) }
        val resonanceRepository = remember { dev.synapse.di.createResonanceRepository(database) }
        val editorViewModel = remember { EditorViewModel(coroutineScope, noteRepository, resonanceRepository) }
        
        EditorScreen(editorViewModel)
    }
}

expect fun getPlatformName(): String
