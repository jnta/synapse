package editor.ast

import androidx.compose.runtime.Immutable

@Immutable
sealed interface NoteNode {
    val id: String
    val attributes: Map<String, String>

    @Immutable
    data class Root(
        override val id: String,
        val children: List<BlockNode>,
        override val attributes: Map<String, String> = emptyMap()
    ) : NoteNode

    @Immutable
    sealed interface BlockNode : NoteNode {
        @Immutable
        data class Heading(
            override val id: String,
            val level: Int,
            val content: List<InlineNode>,
            override val attributes: Map<String, String> = emptyMap()
        ) : BlockNode

        @Immutable
        data class Paragraph(
            override val id: String,
            val content: List<InlineNode>,
            override val attributes: Map<String, String> = emptyMap()
        ) : BlockNode
        
        @Immutable
        data class ListItem(
            override val id: String,
            val content: List<InlineNode>,
            override val attributes: Map<String, String> = emptyMap()
        ) : BlockNode
    }

    @Immutable
    sealed interface InlineNode {
        @Immutable
        data class Text(val value: String) : InlineNode
        @Immutable
        data class Bold(val content: List<InlineNode>) : InlineNode
        @Immutable
        data class Italic(val content: List<InlineNode>) : InlineNode
        @Immutable
        data class Intent(val type: String, val content: List<InlineNode>) : InlineNode
        @Immutable
        data class Link(val url: String, val label: String) : InlineNode
    }
}
