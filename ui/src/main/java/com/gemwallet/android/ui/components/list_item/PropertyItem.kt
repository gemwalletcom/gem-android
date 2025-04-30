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
import com.gemwallet.android.ui.components.InfoButton
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.designsystem.Spacer8


@Composable
fun PropertyItem(
    @StringRes title: Int,
    data: String? = null,
    info: InfoSheetEntity? = null,
) {
    PropertyItem(stringResource(title), data, info)
}

@Composable
fun PropertyItem(
    title: String,
    data: String? = null,
    info: InfoSheetEntity? = null,
) {
    PropertyItem(
        title = { PropertyTitleText(title, info = info) },
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
        modifier = modifier.then(Modifier.height(56.dp)),
        title = title,
        trailing = data,
    )
}

@Composable
fun PropertyTitleText(
    @StringRes text: Int,
    badge: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
    info: InfoSheetEntity? = null,
) {
    PropertyTitleText(stringResource(text), badge, trailing, color, info)
}

@Composable
fun PropertyTitleText(
    text: String,
    badge: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
    info: InfoSheetEntity? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        trailing?.invoke()
        Spacer8()
        Text(
            modifier = Modifier.weight(1f, false),
            text = text,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
        )
        badge?.invoke()
        info?.let { InfoButton(it) }
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