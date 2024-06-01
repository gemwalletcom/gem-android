package com.gemwallet.android.features.settings.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

@Composable
fun LinkItem(
    title: String,
    @DrawableRes icon: Int,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    LinkItem(
        title = title,
        icon = painterResource(id = icon),
        supportingContent,
        trailingContent,
        onClick
    )
}

@Composable
fun LinkItem(
    title: String,
    icon: Painter? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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