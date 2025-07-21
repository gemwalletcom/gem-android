package com.gemwallet.android.ui.components.list_item.property

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.InfoButton
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.ListItem

@Composable
fun PropertyItem(
    @StringRes action: Int,
    actionIconModel: Any? = null,
    data: String? = null,
    onClick: () -> Unit,
) {
    PropertyItem(stringResource(action), actionIconModel, data, onClick)
}

@Composable
fun PropertyItem(
    action: String,
    actionIconModel: Any? = null,
    data: String? = null,
    onClick: () -> Unit,
) {
    PropertyItem(
        modifier = Modifier.clickable(onClick = onClick),
        title = { PropertyTitleText(text = action, trailing = { AsyncImage(actionIconModel, 24.dp) }) },
        data = {
            PropertyDataText(
                data ?: "",
                badge = {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }
    )
}

@Composable
fun PropertyItem(
    @StringRes title: Int,
    data: String? = null,
    info: InfoSheetEntity? = null,
    dataColor: Color = MaterialTheme.colorScheme.secondary,
) {
    PropertyItem(stringResource(title), data, dataColor, info)
}

@Composable
fun PropertyItem(
    @StringRes title: Int,
    @StringRes data: Int,
    info: InfoSheetEntity? = null,
    dataColor: Color = MaterialTheme.colorScheme.secondary,
) {
    PropertyItem(stringResource(title), stringResource(data), dataColor, info)
}

@Composable
fun PropertyItem(
    title: String,
    data: String? = null,
    dataColor: Color = MaterialTheme.colorScheme.secondary,
    info: InfoSheetEntity? = null,
) {
    PropertyItem(
        title = { PropertyTitleText(title, info = info) },
        data = data?.let{ { PropertyDataText(data, color = dataColor) } },
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
        trailing?.let {
            it()
            Spacer8()
        }
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

@Composable
fun DataBadgeChevron(isShowChevron: Boolean = true, content: (@Composable RowScope.() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        content?.let {
            Spacer8()
            it()
        }
        if (isShowChevron) {
            Icon(
                modifier = Modifier.offset(8.dp),
                painter = rememberVectorPainter(image = Icons.Default.ChevronRight),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun DataBadgeChevron(icon: Any, isShowChevron: Boolean = true) {
    DataBadgeChevron(isShowChevron) {
        AsyncImage(icon, size = trailingIconMedium)
    }
}