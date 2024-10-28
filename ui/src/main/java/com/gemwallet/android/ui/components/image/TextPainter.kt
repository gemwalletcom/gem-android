package com.gemwallet.android.ui.components.image

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class TextPainter(
    val circleColor: Color,
    val circleSize : Size,
    val textMeasurer: TextMeasurer,
    val text : String,
) : Painter() {

    val textLayoutResult: TextLayoutResult = textMeasurer.measure(
            text = AnnotatedString(text),
            style = TextStyle(
                Color.Companion.White,
                fontSize = if (text.length == 1) 18.sp else 10.sp,
                fontWeight = if (text.length == 1) FontWeight.Companion.W600 else null,
            ),
        )

    override val intrinsicSize: Size get() = circleSize

    override fun DrawScope.onDraw() {
        //the circle background
        drawCircle(
            color = circleColor,
            radius = this.size.maxDimension / 2
        )

        val textSize = textLayoutResult.size
        //The text
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                (this.size.width - textSize.width) / 2f,
                (this.size.height - textSize.height) / 2f
            )
        )
    }
}