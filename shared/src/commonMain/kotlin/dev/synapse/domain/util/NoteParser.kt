package dev.synapse.domain.util

import dev.synapse.domain.model.Attribute
import dev.synapse.domain.model.Edge

object NoteParser {
    private val WIKILINK_REGEX = "\\[\\[(.*?)\\]\\]".toRegex()
    private val INTENT_FACT_REGEX = "^\\[F\\]".toRegex(RegexOption.MULTILINE)
    private val INTENT_TASK_REGEX = "^\\[T\\]".toRegex(RegexOption.MULTILINE)

    private val ATTRIBUTE_REGEX = "(\\w+)::(.+)".toRegex()
    private val TAG_REGEX = "#(\\w+)".toRegex()

    fun extractAttributes(content: String): List<Attribute> {
        val attributes = mutableListOf<Attribute>()
        
        // Existing intent parsing
        if (INTENT_FACT_REGEX.containsMatchIn(content)) {
            attributes.add(Attribute(generateId(), "intent", "fact"))
        }
        if (INTENT_TASK_REGEX.containsMatchIn(content)) {
            attributes.add(Attribute(generateId(), "intent", "task"))
        }

        // key::value parsing
        ATTRIBUTE_REGEX.findAll(content).forEach { match ->
            val key = match.groupValues[1].trim()
            val value = match.groupValues[2].trim()
            attributes.add(Attribute(generateId(), key, value))
        }

        // #tag parsing
        TAG_REGEX.findAll(content).forEach { match ->
            val tag = match.groupValues[1].trim()
            attributes.add(Attribute(generateId(), "tag", tag))
        }

        return attributes
    }

    fun extractEdges(sourceId: String, content: String): List<Edge> {
        return WIKILINK_REGEX.findAll(content).map { match ->
            val targetTitle = match.groupValues[1]
            Edge(
                id = generateId(),
                sourceId = sourceId,
                targetId = targetTitle, // Resolution happens at repository or service level
                label = "links"
            )
        }.toList()
    }

    private fun generateId(): String = UUIDv7.generate()
}
