package editor.ast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val H1_PREFIX_LEN = 2
private const val H2_PREFIX_LEN = 3
private const val H3_PREFIX_LEN = 4
private const val LIST_PREFIX_LEN = 2

object AstParser {
    
    suspend fun parseBlock(id: String, rawContent: String): NoteNode.BlockNode = withContext(Dispatchers.Default) {
        val attributes = extractAttributes(rawContent)
        val cleanContent = removeAttributes(rawContent)
        
        when {
            cleanContent.startsWith("# ") -> {
                NoteNode.BlockNode.Heading(
                    id = id,
                    level = 1,
                    content = parseInline(cleanContent.substring(H1_PREFIX_LEN)),
                    attributes = attributes
                )
            }
            cleanContent.startsWith("## ") -> {
                NoteNode.BlockNode.Heading(
                    id = id,
                    level = 2,
                    content = parseInline(cleanContent.substring(H2_PREFIX_LEN)),
                    attributes = attributes
                )
            }
            cleanContent.startsWith("### ") -> {
                NoteNode.BlockNode.Heading(
                    id = id,
                    level = 3,
                    content = parseInline(cleanContent.substring(H3_PREFIX_LEN)),
                    attributes = attributes
                )
            }
            cleanContent.startsWith("- ") || cleanContent.startsWith("* ") -> {
                NoteNode.BlockNode.ListItem(
                    id = id,
                    content = parseInline(cleanContent.substring(LIST_PREFIX_LEN)),
                    attributes = attributes
                )
            }
            else -> {
                NoteNode.BlockNode.Paragraph(
                    id = id,
                    content = parseInline(cleanContent),
                    attributes = attributes
                )
            }
        }
    }
    
    private fun parseInline(text: String): List<NoteNode.InlineNode> {
        val nodes = mutableListOf<NoteNode.InlineNode>()
        var remaining = text
        
        while (remaining.isNotEmpty()) {
            val boldMatch = Regex("""\*\*(.*?)\*\*""").find(remaining)
            val italicMatch = Regex("""_(.*?)_""").find(remaining)
            
            val nextMatch = listOfNotNull(boldMatch, italicMatch).minByOrNull { it.range.first }
            
            if (nextMatch == null) {
                nodes.add(NoteNode.InlineNode.Text(remaining))
                break
            }
            
            if (nextMatch.range.first > 0) {
                nodes.add(NoteNode.InlineNode.Text(remaining.substring(0, nextMatch.range.first)))
            }
            
            val innerText = nextMatch.groupValues[1]
            when (nextMatch.value.first()) {
                '*' -> nodes.add(NoteNode.InlineNode.Bold(parseInline(innerText)))
                '_' -> nodes.add(NoteNode.InlineNode.Italic(parseInline(innerText)))
            }
            
            remaining = remaining.substring(nextMatch.range.last + 1)
        }
        
        return if (nodes.isEmpty() && text.isNotEmpty()) listOf(NoteNode.InlineNode.Text(text)) else nodes
    }
    
    fun extractAttributes(text: String): Map<String, String> {
        val attrRegex = Regex("\\[([^\\]\\s:]+)::(.*?)\\]")
        val map = mutableMapOf<String, String>()
        attrRegex.findAll(text).forEach { match ->
            map[match.groupValues[1]] = match.groupValues[2]
        }
        return map
    }
    
    private fun removeAttributes(text: String): String {
        val attrRegex = Regex("\\[([^\\]\\s:]+)::(.*?)\\]")
        return text.replace(attrRegex, "").trim()
    }
}
