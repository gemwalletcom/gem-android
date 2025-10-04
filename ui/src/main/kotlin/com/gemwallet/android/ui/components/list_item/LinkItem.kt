package com.gemwallet.android.ui.components.list_item

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.models.ListPosition

@Composable
fun LinkItem(
    title: String,
    @DrawableRes icon: Int,
    listPosition: ListPosition = ListPosition.Middle,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    LinkItem(
        title = title,
        painter = painterResource(id = icon),
        listPosition = listPosition,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

@Composable
fun LinkItem(
    title: String,
    painter: Painter? = null,
    listPosition: ListPosition = ListPosition.Middle,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val minHeight = if (supportingContent == null) 72.dp else 88.dp
    ListItem(
        modifier = Modifier
            .defaultMinSize(minHeight = minHeight)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        leading = if (painter != null) {
            {
                Image(painter = painter, contentDescription = "setting_item")
            }
        } else {
            null
        },
        title = { Text(text = title) },
        subtitle = supportingContent,
        trailing = trailingContent,
        listPosition = listPosition,
    )
}