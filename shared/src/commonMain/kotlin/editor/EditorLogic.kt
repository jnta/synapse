package editor

import editor.ast.NoteNode


sealed class BlockAction {
    data class Add(val afterId: String) : BlockAction()
    data class Remove(val id: String) : BlockAction()
    data class Move(val fromIndex: Int, val toIndex: Int) : BlockAction()
}

object EditorLogic {
    const val MAX_TITLE_LENGTH = 50

    fun generateId(): String = dev.synapse.domain.util.UUIDv7.generate()

    fun createInitialBlock(): NoteBlock = NoteBlock(
        id = generateId(), 
        content = "", 
        astNode = NoteNode.BlockNode.Paragraph(generateId(), listOf())
    )

    fun extractAttributes(content: String): List<String> {
        val attributes = mutableListOf<String>()
        if (content.startsWith("[C]")) attributes.add("Capture")
        if (content.startsWith("[R]")) attributes.add("Reference")
        if (content.startsWith("[S]")) attributes.add("Synthesis")
        return attributes
    }

    fun mutateBlocks(state: EditorUiState, action: BlockAction): EditorUiState {
        val newBlocks = state.blocks.toMutableList()
        return when (action) {
            is BlockAction.Add -> {
                val index = state.blocks.indexOfFirst { it.id == action.afterId }
                if (index != -1) {
                    val newBlock = createInitialBlock()
                    newBlocks.add(index + 1, newBlock)
                    state.copy(blocks = newBlocks, focusedBlockId = newBlock.id)
                } else state
            }
            is BlockAction.Remove -> {
                if (newBlocks.size > 1) {
                    newBlocks.removeAll { it.id == action.id }
                    state.copy(blocks = newBlocks)
                } else state
            }
            is BlockAction.Move -> {
                if (action.fromIndex in newBlocks.indices && action.toIndex in newBlocks.indices) {
                    val block = newBlocks.removeAt(action.fromIndex)
                    newBlocks.add(action.toIndex, block)
                    state.copy(blocks = newBlocks)
                } else state
            }
        }
    }
    fun updateNavigationStack(currentStack: List<String>, nextNoteId: String, limit: Int = 10): List<String> {
        if (currentStack.contains(nextNoteId)) return currentStack
        
        val temp = currentStack + nextNoteId
        return if (temp.size > limit) temp.drop(temp.size - limit) else temp
    }
}
