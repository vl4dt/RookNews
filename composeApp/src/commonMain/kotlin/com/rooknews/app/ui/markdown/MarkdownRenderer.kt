package com.rooknews.app.ui.markdown

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.rooknews.app.ui.theme.RookNewsTheme

/**
 * Renders Markdown text as a Column of styled text.
 * Supports: headings, bold, italic, code, lists, blockquotes, horizontal rules.
 *
 * Note: This composable does NOT scroll internally. The caller should handle scrolling.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography
    val parsedElements = parseMarkdown(markdown)

    Column(modifier = modifier) {
        parsedElements.forEach { element ->
            when (element) {
                is MarkdownHeading -> {
                    when (element.level) {
                        1 -> Text(element.content, style = typography.headlineSmall, color = colors.onSurface, modifier = Modifier.padding(vertical = 8.dp))
                        2 -> Text(element.content, style = typography.titleLarge, color = colors.onSurface, modifier = Modifier.padding(vertical = 6.dp))
                        3 -> Text(element.content, style = typography.titleMedium, color = colors.onSurface, modifier = Modifier.padding(vertical = 4.dp))
                        else -> Text(element.content, style = typography.titleSmall, color = colors.onSurface, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                is MarkdownParagraph -> {
                    MarkdownInlineText(element.content, style = typography.bodyLarge, color = colors.onSurface)
                }
                is MarkdownBlockquote -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                    ) {
                        Text(element.content, style = typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(12.dp))
                    }
                }
                is MarkdownHorizontalRule -> {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                }
                is MarkdownUnorderedList -> {
                    element.items.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", style = typography.bodyLarge, color = colors.primary)
                            MarkdownInlineText(item, style = typography.bodyLarge, color = colors.onSurface)
                        }
                    }
                }
                is MarkdownOrderedList -> {
                    element.items.forEachIndexed { index, item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("${index + 1}. ", style = typography.bodyLarge, color = colors.primary)
                            MarkdownInlineText(item, style = typography.bodyLarge, color = colors.onSurface)
                        }
                    }
                }
                is MarkdownCodeBlock -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                    ) {
                        Text(
                            element.content,
                            style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders inline Markdown (bold, italic, code) as a Compose Text with SpanStyles.
 */
@Composable
private fun MarkdownInlineText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color
) {
    val annotatedString = buildAnnotatedString(text, style = style)
    Text(text = annotatedString, style = style, color = color)
}

private fun buildAnnotatedString(markdown: String, style: androidx.compose.ui.text.TextStyle): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var remaining = markdown

    while (remaining.isNotEmpty()) {
        // Code span (backtick)
        val codeIdx = remaining.indexOf("`")
        if (codeIdx >= 0) {
            builder.append(remaining.take(codeIdx))
            remaining = remaining.drop(codeIdx + 1)
            val endIdx = remaining.indexOf("`")
            if (endIdx >= 0) {
                builder.pushStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color(0xFFEEEEEE),
                    fontSize = 12.sp
                ))
                builder.append(remaining.take(endIdx))
                builder.pop()
                remaining = remaining.drop(endIdx + 1)
            } else {
                builder.append("`")
            }
            continue
        }

        // Bold (double asterisk)
        val boldIdx = remaining.indexOf("**")
        if (boldIdx >= 0) {
            builder.append(remaining.take(boldIdx))
            remaining = remaining.drop(boldIdx + 2)
            val endBold = remaining.indexOf("**")
            if (endBold >= 0) {
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(remaining.take(endBold))
                builder.pop()
                remaining = remaining.drop(endBold + 2)
            } else {
                builder.append("**")
            }
            continue
        }

        // Italic (single asterisk, not followed by another asterisk or backtick)
        val italicIdx = remaining.indexOf("*")
        if (italicIdx >= 0 && italicIdx + 1 < remaining.length && remaining[italicIdx + 1] != '*' && remaining[italicIdx + 1] != '`') {
            builder.append(remaining.take(italicIdx))
            remaining = remaining.drop(italicIdx + 1)
            val endItalic = remaining.indexOf("*")
            if (endItalic > 0) {
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                builder.append(remaining.take(endItalic))
                builder.pop()
                remaining = remaining.drop(endItalic + 1)
            } else {
                builder.append("*")
            }
            continue
        }

        // No more special chars
        builder.append(remaining)
        remaining = ""
    }

    return builder.toAnnotatedString()
}

// --- Markdown parsing ---

private sealed interface MarkdownElement

private data class MarkdownHeading(val level: Int, val content: String) : MarkdownElement
private data class MarkdownParagraph(val content: String) : MarkdownElement
private data class MarkdownBlockquote(val content: String) : MarkdownElement
private data class MarkdownHorizontalRule(val text: String) : MarkdownElement
private data class MarkdownUnorderedList(val items: List<String>) : MarkdownElement
private data class MarkdownOrderedList(val items: List<String>) : MarkdownElement
private data class MarkdownCodeBlock(val content: String) : MarkdownElement

private fun parseMarkdown(markdown: String): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    val lines = markdown.split('\n')
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block (```)
        if (line.trim().startsWith("```")) {
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            i++ // skip closing ```
            elements.add(MarkdownCodeBlock(codeLines.joinToString("\n")))
            continue
        }

        // Heading
        val headingMatch = Regex("^(#{1,6})\\s+(.*)").find(line)
        if (headingMatch != null) {
            elements.add(MarkdownHeading(headingMatch.groupValues[1].length, headingMatch.groupValues[2].trim()))
            i++
            continue
        }

        // Blockquote
        if (line.trim().startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines.add(lines[i].trim().substringAfter(">").trim())
                i++
            }
            elements.add(MarkdownBlockquote(quoteLines.joinToString("\n")))
            continue
        }

        // Horizontal rule
        if (line.trim().matches(Regex("^(-{3,}|\\*{3,}|_{3,})$"))) {
            elements.add(MarkdownHorizontalRule(line.trim()))
            i++
            continue
        }

        // Unordered list
        if (line.trim().matches(Regex("^[-*+]\\s+.*"))) {
            val items = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().matches(Regex("^[-*+]\\s+.*"))) {
                val itemText = lines[i].trim()
                val afterMarker = Regex("^[+-]\\s+|^[*]\\s+").replace(itemText, "")
                items.add(afterMarker.trim())
                i++
            }
            elements.add(MarkdownUnorderedList(items))
            continue
        }

        // Ordered list
        if (line.trim().matches(Regex("^\\d+\\.\\s+.*"))) {
            val items = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s+.*"))) {
                val afterMarker = Regex("^\\d+\\.\\s+").replace(lines[i].trim(), "")
                items.add(afterMarker.trim())
                i++
            }
            elements.add(MarkdownOrderedList(items))
            continue
        }

        // Empty line — skip
        if (line.trim().isEmpty()) {
            i++
            continue
        }

        // Regular paragraph — collect consecutive non-empty, non-special lines
        val paragraphLines = mutableListOf(line)
        i++
        while (i < lines.size && lines[i].trim().isNotEmpty() &&
               !lines[i].trim().startsWith("#") &&
               !lines[i].trim().startsWith(">") &&
               !lines[i].trim().matches(Regex("^(-{3,}|\\*{3,}|_{3,})$")) &&
               !lines[i].trim().matches(Regex("^[-*+]\\s+.*")) &&
               !lines[i].trim().matches(Regex("^\\d+\\.\\s+.*"))) {
            paragraphLines.add(lines[i])
            i++
        }
        elements.add(MarkdownParagraph(paragraphLines.joinToString(" ")))
    }

    return elements
}
