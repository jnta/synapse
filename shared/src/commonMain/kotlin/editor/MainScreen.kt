package editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: EditorViewModel) {
    val state by viewModel.state.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(SynapseColors.Background)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                    when (event.key) {
                        Key.N -> { viewModel.onEvent(EditorUiEvent.CreateNewNote); true }
                        Key.S -> { viewModel.onEvent(EditorUiEvent.SaveCurrentNote); true }
                        Key.I -> { viewModel.onEvent(EditorUiEvent.Resonate); true }
                        Key.Enter -> {
                            state.focusedBlockId?.let { viewModel.onEvent(EditorUiEvent.NoteOverflow(it)) }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        AnimatedVisibility(visible = state.isSidebarVisible) {
            LeftNav(state = state, onEvent = viewModel::onEvent)
        }

        Box(modifier = Modifier.weight(1f)) {
            when (state.currentDestination) {
                "All Notes" -> VaultScreen(state = state, onEvent = viewModel::onEvent)
                "Editor" -> NoteEditorScreen(state = state, onEvent = viewModel::onEvent)
                else -> VaultScreen(state = state, onEvent = viewModel::onEvent)
            }
        }

        AnimatedVisibility(visible = state.isContextPanelVisible && state.currentDestination == "Editor") {
            ContextPanel(state = state, onEvent = viewModel::onEvent)
        }
    }

    if (state.showResonanceFilter) {
        ResonanceFilterModal(state, viewModel::onEvent)
    }
}
