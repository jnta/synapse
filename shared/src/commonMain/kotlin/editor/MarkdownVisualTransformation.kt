package editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MarkdownVisualTransformation(private val shouldMask: Boolean = false) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        
        // Bold: **text**
        Regex("\\*\\*(.*?)\\*\\*").findAll(text.text).forEach { match ->
            builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            builder.addStyle(SpanStyle(color = Color.LightGray), match.range.first, match.range.first + 2)
            builder.addStyle(SpanStyle(color = Color.LightGray), match.range.last - 1, match.range.last + 1)
        }
        
        // Italic: _text_
        Regex("_(.*?)_").findAll(text.text).forEach { match ->
            builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
            builder.addStyle(SpanStyle(color = Color.LightGray), match.range.first, match.range.first + 1)
            builder.addStyle(SpanStyle(color = Color.LightGray), match.range.last, match.range.last + 1)
        }

        // Headings: #, ##, ###
        Regex("^(#+ )").findAll(text.text).forEach { match ->
            builder.addStyle(SpanStyle(color = Color.LightGray.copy(alpha = 0.5f)), match.range.first, match.range.last)
        }

        // List items: - , * 
        Regex("^([-*] )").findAll(text.text).forEach { match ->
            builder.addStyle(SpanStyle(color = Color.LightGray.copy(alpha = 0.5f)), match.range.first, match.range.last)
        }

        // Color Initial Intents (e.g. [F], [T])
        val intentRegex = Regex("^\\[[FTHP]\\]")
        intentRegex.find(text.text)?.let { match ->
            builder.addStyle(
                style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Fog of War: Mask non-bold text if enabled
        if (shouldMask) {
            // Apply a global mask to the entire text
            builder.addStyle(
                SpanStyle(color = Color.LightGray.copy(alpha = 0.2f), background = Color.LightGray.copy(alpha = 0.1f)),
                0,
                text.text.length
            )
            
            // "Unlock" bold sections by re-applying original colors/alpha
            Regex("\\*\\*(.*?)\\*\\*").findAll(text.text).forEach { match ->
                builder.addStyle(
                    SpanStyle(color = Color.Unspecified), // Reset to default color
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
