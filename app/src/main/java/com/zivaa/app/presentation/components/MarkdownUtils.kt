package com.zivaa.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A lightweight, native Jetpack Compose markdown parser.
 * It handles **bold**, *italics*, ### headings, --- dividers, and bulleted/numbered lists.
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val lines = text.split("\n")
        var currentBlockBuilder = AnnotatedString.Builder()
        var hasContentInBlock = false

        @Composable
        fun flushBlock() {
            if (hasContentInBlock) {
                Text(
                    text = currentBlockBuilder.toAnnotatedString(),
                    color = color,
                    modifier = Modifier.padding(bottom = 8.dp),
                    lineHeight = 22.sp
                )
                currentBlockBuilder = AnnotatedString.Builder()
                hasContentInBlock = false
            }
        }

        for (line in lines) {
            val trimmedLine = line.trimStart()

            if (trimmedLine.isEmpty()) {
                flushBlock()
                continue
            }

            // 1. Horizontal Rules
            if (trimmedLine == "---" || trimmedLine == "***" || trimmedLine == "___") {
                flushBlock()
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = color.copy(alpha = 0.2f)
                )
                continue
            }

            // 2. Headings
            val h1 = Regex("^# (.*)").find(trimmedLine)
            val h2 = Regex("^## (.*)").find(trimmedLine)
            val h3 = Regex("^### (.*)").find(trimmedLine)
            val h4 = Regex("^#### (.*)").find(trimmedLine)
            val h5 = Regex("^##### (.*)").find(trimmedLine)

            if (h1 != null || h2 != null || h3 != null || h4 != null || h5 != null) {
                flushBlock()
                val headingText = h1?.groupValues?.get(1) ?: h2?.groupValues?.get(1) ?: h3?.groupValues?.get(1) ?: h4?.groupValues?.get(1) ?: h5?.groupValues?.get(1) ?: ""
                val fontSize = when {
                    h1 != null -> 24.sp
                    h2 != null -> 20.sp
                    h3 != null -> 18.sp
                    h4 != null -> 16.sp
                    else -> 14.sp
                }

                val headingBuilder = AnnotatedString.Builder()
                headingBuilder.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize)) {
                    parseInlineMarkdown(headingText, this)
                }

                Text(
                    text = headingBuilder.toAnnotatedString(),
                    color = color,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    lineHeight = (fontSize.value * 1.2f).sp
                )
                continue
            }

            // 3. Bullets
            val isBullet = trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith("• ")
            val isNumbered = trimmedLine.matches(Regex("^[0-9]+\\. .*"))

            if (isBullet || isNumbered) {
                flushBlock()
                
                val bulletBuilder = AnnotatedString.Builder()
                val processedLine = when {
                    trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> "• " + trimmedLine.substring(2)
                    trimmedLine.startsWith("• ") -> trimmedLine
                    else -> trimmedLine
                }

                bulletBuilder.withStyle(style = ParagraphStyle(textIndent = TextIndent(restLine = 20.sp))) {
                    parseInlineMarkdown(processedLine, this)
                }

                Text(
                    text = bulletBuilder.toAnnotatedString(),
                    color = color,
                    modifier = Modifier.padding(bottom = 4.dp),
                    lineHeight = 22.sp
                )
                continue
            }

            // 4. Regular Text
            if (hasContentInBlock) {
                currentBlockBuilder.append("\n")
            }
            parseInlineMarkdown(line, currentBlockBuilder)
            hasContentInBlock = true
        }

        flushBlock()
    }
}

private fun parseInlineMarkdown(line: String, builder: AnnotatedString.Builder) {
    val regex = Regex("\\*\\*(.*?)\\*\\*|\\*(.*?)\\*")
    var lastIndex = 0

    regex.findAll(line).forEach { matchResult ->
        val beforeMatch = line.substring(lastIndex, matchResult.range.first)
        if (beforeMatch.isNotEmpty()) {
            builder.append(beforeMatch)
        }

        val boldText = matchResult.groups[1]?.value
        val italicText = matchResult.groups[2]?.value

        when {
            boldText != null -> {
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldText)
                }
            }
            italicText != null -> {
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicText)
                }
            }
        }

        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < line.length) {
        builder.append(line.substring(lastIndex))
    }
}
