package com.gemwallet.android.features.settings.settings.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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