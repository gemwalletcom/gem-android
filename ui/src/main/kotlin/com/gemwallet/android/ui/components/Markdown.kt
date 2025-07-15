package com.gemwallet.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun parseMarkdownToAnnotatedString(markdown: String): AnnotatedString {
    // Define regex patterns
    val linkRegex = """\[(.*?)\]\((.*?)\)""".toRegex()
    val boldRegex = """\*\*(.*?)\*\*""".toRegex()
    val italicRegex = """\*(.*?)\*""".toRegex()
    val codeBlockRegex = """```([\s\S]*?)```""".toRegex()
    val inlineCodeRegex = """`(.*?)`""".toRegex()
    val headingRegex = """^(#{1,2})\s*(.*)""".toRegex(RegexOption.MULTILINE)
    val listRegex = """^- (.*)""".toRegex(RegexOption.MULTILINE)
    val blockquoteRegex = """^>\s+(.*)""".toRegex(RegexOption.MULTILINE)  // NEW

    val tokens = mutableListOf<MarkdownToken>()
    fun addMatches(pattern: Regex, type: TokenType, groupCount: Int) {
        pattern.findAll(markdown).forEach { result ->
            val matchedGroups = (1..groupCount).map { i -> result.groups[i]?.value ?: "" }
            tokens += MarkdownToken(
                type = type,
                start = result.range.first,
                end = result.range.last + 1,
                groups = matchedGroups
            )
        }
    }

    // Collect tokens for each pattern
    addMatches(codeBlockRegex, TokenType.CODE_BLOCK, 1)
    addMatches(inlineCodeRegex, TokenType.INLINE_CODE, 1)
    addMatches(linkRegex, TokenType.LINK, 2)
    addMatches(boldRegex, TokenType.BOLD, 1)
    addMatches(italicRegex, TokenType.ITALIC, 1)
    addMatches(headingRegex, TokenType.HEADING, 2)
    addMatches(listRegex, TokenType.LIST, 1)
    addMatches(blockquoteRegex, TokenType.BLOCKQUOTE, 1)

    tokens.sortBy { it.start }

    val builder = AnnotatedString.Builder()
    var currentIndex = 0

    fun appendGapText(upTo: Int) {
        if (currentIndex < upTo) {
            builder.append(markdown.substring(currentIndex, upTo))
            currentIndex = upTo
        }
    }

    for (token in tokens) {
        if (token.start < currentIndex) continue
        appendGapText(token.start)

        when (token.type) {
            TokenType.CODE_BLOCK -> {
                val codeContent = token.groups[0].trim()
                val styleStart = builder.length
                builder.append(codeContent)
                builder.addStyle(
                    SpanStyle(
                        background = Color(0xFFEFEFEF),
                        color = Color(0xFF333333),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    styleStart,
                    builder.length
                )
            }
            TokenType.INLINE_CODE -> {
                val codeContent = token.groups[0]
                val styleStart = builder.length
                builder.append(codeContent)
                builder.addStyle(
                    SpanStyle(
                        background = Color.LightGray,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    styleStart,
                    builder.length
                )
            }
            TokenType.LINK -> {
                val (linkText, linkUrl) = token.groups
                val styleStart = builder.length
                builder.append(linkText)
                builder.addStyle(
                    SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    ),
                    styleStart,
                    builder.length
                )
                // Attach a string annotation with tag = "URL"
                builder.addStringAnnotation(
                    tag = "URL",
                    annotation = linkUrl,
                    start = styleStart,
                    end = builder.length
                )
            }
            TokenType.BOLD -> {
                val boldContent = token.groups[0]
                val styleStart = builder.length
                builder.append(boldContent)
                builder.addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    styleStart,
                    builder.length
                )
            }
            TokenType.ITALIC -> {
                val italicContent = token.groups[0]
                val styleStart = builder.length
                builder.append(italicContent)
                builder.addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic),
                    styleStart,
                    builder.length
                )
            }
            TokenType.HEADING -> {
                val headingLevel = token.groups[0].length // # or ##
                val headingText = token.groups[1]
                val styleStart = builder.length
                builder.append(headingText)
                builder.addStyle(
                    SpanStyle(
                        fontSize = if (headingLevel == 1) 26.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (headingLevel == 1) Color(0xFFA75CF2) else Color(0xFF48D883)
                    ),
                    styleStart,
                    builder.length
                )
            }
            TokenType.LIST -> {
                val listItem = token.groups[0]
                builder.append("• $listItem\n")
            }
            TokenType.BLOCKQUOTE -> {
                val quoteText = token.groups[0]
                val styleStart = builder.length
                builder.append(quoteText)
                builder.addStyle(
                    SpanStyle(
                        background = Color(0xFFE0E0E0),
                        fontStyle = FontStyle.Italic
                    ),
                    styleStart,
                    builder.length
                )
                builder.append("\n")
            }
        }
        currentIndex = token.end
    }

    appendGapText(markdown.length)

    return builder.toAnnotatedString()
}

private data class MarkdownToken(
    val type: TokenType,
    val start: Int,
    val end: Int,
    val groups: List<String>
)


private enum class TokenType {
    CODE_BLOCK,
    INLINE_CODE,
    LINK,
    BOLD,
    ITALIC,
    HEADING,
    LIST,
    BLOCKQUOTE
}