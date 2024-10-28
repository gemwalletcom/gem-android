package com.gemwallet.android.ui.components.image

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import coil.compose.AsyncImage
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
    AsyncImage(
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