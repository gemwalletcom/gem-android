package com.gemwallet.android.features.settings.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun LinkItem(
    title: String,
    @DrawableRes icon: Int,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    LinkItem(
        title = title,
        icon = painterResource(id = icon),
        supportingContent,
        trailingContent,
        onLongClick,
        onClick
    )
}

@Composable
fun LinkItem(
    title: String,
    icon: Painter? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingItem(
            headLine = { Text(text = title) },
            supportingContent = supportingContent,
            leadingContent = if (icon != null) {
                {
                    Image(painter = icon, contentDescription = "setting_item")
                }
            } else {
                null
            },
            trailingContent = trailingContent,
        )
    }
}

@Composable
fun SettingItem(
    headLine: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val minHeight = if (supportingContent == null) 72.dp else 88.dp
    ListItem(
        modifier = Modifier
            .defaultMinSize(minHeight = minHeight),
        headlineContent = headLine,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}