package com.gemwallet.android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

@Composable
fun AsyncImage(
    model: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
    errorImageVector: ImageVector? = null,
) {
    val placeholder = if (placeholderText.isNullOrEmpty()) {
        null
    } else {
        TextPainter(
            circleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
            textMeasurer = rememberTextMeasurer(),
            text = placeholderText,
            circleSize = Size(200f, 200f)
        )
    }

    val error = if (errorImageVector == null) {
        placeholder
    } else {
        rememberVectorPainter(image = errorImageVector)
    }
    coil.compose.AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .diskCachePolicy(policy = CachePolicy.ENABLED)
            .networkCachePolicy(policy = CachePolicy.ENABLED)
            .transformations(CircleCropTransformation())
            .build(),
        placeholder = placeholder,
        error = error,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

class TextPainter(
    val circleColor: Color,
    val circleSize : Size,
    val textMeasurer: TextMeasurer,
    val text : String,
) : Painter() {

    val textLayoutResult: TextLayoutResult = textMeasurer.measure(
            text = AnnotatedString(text),
            style = TextStyle(
                Color.White,
                fontSize = if (text.length == 1) 18.sp else 10.sp,
                fontWeight = if (text.length == 1) FontWeight.W600 else null,
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