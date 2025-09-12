package com.gemwallet.android.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import java.text.BreakIterator

// Source - https://github.com/mataku/MiddleEllipsisText/tree/develop
@Composable
fun MiddleEllipsisText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    ellipsisChar: Char = '.',
    ellipsisCharCount: Int = 3
) {
    if (text.isEmpty()) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            softWrap = softWrap,
            onTextLayout = onTextLayout,
            style = style
        )
    } else {
        var textLayoutResult: TextLayoutResult? = null
        val ellipsisText = ellipsisChar.toString().repeat(ellipsisCharCount)

        val breakIterator = BreakIterator.getCharacterInstance()
        breakIterator.setText(text)
        val charSplitIndexList = mutableListOf<Int>()
        while (breakIterator.next() != BreakIterator.DONE) {
            val index = breakIterator.current()
            charSplitIndexList.add(index)
        }
        SubcomposeLayout(modifier) { constraints ->
            subcompose("MiddleEllipsisText_calculate") {
                Text(
                    text = text + ellipsisChar,
                    color = color,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    softWrap = softWrap,
                    onTextLayout = { textLayoutResult = it },
                    style = style
                )
            }[0].measure(Constraints())

            textLayoutResult ?: return@SubcomposeLayout layout(0, 0) {}

            val placeable = subcompose("MiddleEllipsisText_apply") {
                val combinedText = remember(text, ellipsisText, textLayoutResult) {
                    if (textLayoutResult!!.getBoundingBox(text.lastIndex).right <= constraints.maxWidth) {
                        text
                    } else {
                        val ellipsisCharWidth = textLayoutResult!!.getBoundingBox(text.lastIndex + 1).width
                        val ellipsisTextWidth: Float = ellipsisCharWidth * ellipsisCharCount
                        val remainingWidth = constraints.maxWidth - ellipsisTextWidth
                        var leftPoint = 0
                        var rightPoint = text.lastIndex
                        var leftTextWidth = 0F
                        var rightTextWidth = 0F
                        var realLeftIndex = 0
                        var realRightIndex = charSplitIndexList.lastIndex

                        val textFromStart = mutableListOf<Char>()
                        val textFromEnd = mutableListOf<Char>()

                        run {
                            repeat(charSplitIndexList.size) {
                                if (leftPoint >= rightPoint) {
                                    return@run
                                }

                                val leftTextBoundingBox = textLayoutResult!!.getBoundingBox(leftPoint)
                                val rightTextBoundingBox = textLayoutResult!!.getBoundingBox(rightPoint)

                                // For multibyte string handling
                                if (leftTextWidth <= rightTextWidth && leftTextWidth + leftTextBoundingBox.width + rightTextWidth <= remainingWidth) {
                                    val remainingTargetCodePoints = if (realLeftIndex == 0) {
                                        charSplitIndexList[realLeftIndex]
                                    } else {
                                        charSplitIndexList[realLeftIndex] - charSplitIndexList[realLeftIndex - 1]
                                    }
                                    val targetText = mutableListOf<Char>()
                                    // multiple code points handling (e.g. flag emoji)
                                    repeat(remainingTargetCodePoints) {
                                        runCatching {
                                            targetText.add(text[leftPoint])
                                            val leftTextBoundingBoxWidth =
                                                textLayoutResult!!.getBoundingBox(leftPoint).width
                                            leftTextWidth += leftTextBoundingBoxWidth
                                            leftPoint += 1
                                        }.onFailure {
                                            return@run
                                        }
                                    }
                                    if (leftTextWidth + rightTextWidth <= remainingWidth) {
                                        textFromStart.addAll(targetText)
                                        realLeftIndex += 1
                                    }
                                } else if (leftTextWidth >= rightTextWidth && leftTextWidth + rightTextWidth + rightTextBoundingBox.width <= remainingWidth) {
                                    val remainingTargetCodePoints =
                                        charSplitIndexList[realRightIndex] - charSplitIndexList[realRightIndex - 1]
                                    val targetText = mutableListOf<Char>()
                                    // multiple code points handling (e.g. flag emoji)
                                    repeat(remainingTargetCodePoints) {
                                        runCatching {
                                            targetText.add(0, text[rightPoint])
                                            val rightTextBoundingBoxWidth =
                                                textLayoutResult!!.getBoundingBox(rightPoint).width
                                            rightTextWidth += rightTextBoundingBoxWidth
                                            rightPoint -= 1
                                        }.onFailure {
                                            return@run
                                        }
                                    }
                                    if (leftTextWidth + rightTextWidth <= remainingWidth) {
                                        textFromEnd.addAll(0, targetText)
                                        realRightIndex -= 1
                                    }
                                } else {
                                    return@run
                                }
                            }
                        }

                        textFromStart.joinToString(separator = "") + ellipsisText + textFromEnd
                            .joinToString(
                                separator = ""
                            )
                    }
                }
                Text(
                    text = combinedText,
                    color = color,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    softWrap = softWrap,
                    maxLines = 1,
                    onTextLayout = onTextLayout,
                    style = style
                )
            }[0].measure(constraints)

            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }
}