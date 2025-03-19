package com.gemwallet.android.ui.components.list_item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun PropertyItem(@StringRes title: Int, data: String? = null) {
    PropertyItem(stringResource(title), data)
}

@Composable
fun PropertyItem(
    title: String,
    data: String? = null,
) {
    PropertyItem(
        title = { PropertyTitleText(title) },
        data = data?.let{ { PropertyDataText(data) } },
    )
}

@Composable
fun PropertyItem(
    title: @Composable () -> Unit,
    data: @Composable (RowScope.() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.height(56.dp),
        title = title,
        trailing = data,
    )
}

@Composable
fun PropertyTitleText(
    @StringRes text: Int,
    badge: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    PropertyTitleText(stringResource(text), badge, color)
}

@Composable
fun PropertyTitleText(
    text: String,
    badge: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(1f, false),
            text = text,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
        )
        badge?.invoke()
    }
}

@Composable
fun RowScope.PropertyDataText(
    text: String,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            color = color,
            style = MaterialTheme.typography.bodyLarge,
        )
        badge?.invoke()
    }
}