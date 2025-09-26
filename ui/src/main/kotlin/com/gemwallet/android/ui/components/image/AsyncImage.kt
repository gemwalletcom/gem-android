package com.gemwallet.android.ui.components.image

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import coil3.transform.Transformation
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.ui.theme.iconSize
import com.wallet.core.primitives.Asset

@Composable
fun AsyncImage(
    model: Any?,
    size: Dp? = iconSize,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    placeholderText: String? = null,
    errorImageVector: ImageVector? = null,
    transformation: Transformation? = CircleCropTransformation()
) {
    if (model == null) {
        return
    }
    if (model is Asset) {
        AsyncImage(model, size ?: iconSize, modifier, placeholderText, errorImageVector)
        return
    }
    val placeholder = if (placeholderText.isNullOrEmpty()) {
        null
    } else {
        TextPainter(
            circleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
            textMeasurer = rememberTextMeasurer(),
            text = placeholderText,
            circleSize = Size(400f, 400f)
        )
    }

    val error = if (errorImageVector == null) {
        placeholder
    } else {
        rememberVectorPainter(image = errorImageVector)
    }
    val modelBuilder = ImageRequest.Builder(LocalContext.current)
        .data(model)
        .diskCachePolicy(policy = CachePolicy.ENABLED)
        .networkCachePolicy(policy = CachePolicy.ENABLED)
    if (transformation != null) {
        modelBuilder.transformations(transformation)
    }
    AsyncImage(
        model = modelBuilder.build(),
        placeholder = placeholder,
        error = error,
        contentDescription = contentDescription,
        modifier = size?.let { modifier.size(size) } ?: modifier,
    )
}

@Composable
fun AsyncImage(
    model: Asset,
    size: Dp = iconSize,
    modifier: Modifier = Modifier,
    placeholderText: String? = model.symbol,
    errorImageVector: ImageVector? = null,
) {
    AsyncImage(
        model = model.getIconUrl(),
        size = size,
        contentDescription = "asset_icon",
        modifier = modifier,
        placeholderText = placeholderText,
        errorImageVector = errorImageVector
    )
}