package com.gemwallet.android.ui.components.image

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.listItemIconSize
import com.gemwallet.android.ui.components.designsystem.listItemSupportIconSize
import com.wallet.core.primitives.Asset

@Composable
fun IconWithBadge(
    asset: Asset,
    size: Dp = listItemIconSize,
    supportSize: Dp = listItemSupportIconSize,
) {
    IconWithBadge(
        icon = asset.getIconUrl(),
        supportIcon = asset.getSupportIconUrl(),
        placeholder = asset.type.string,
        size = size,
        supportSize = supportSize,
    )
}

@Composable
fun IconWithBadge(
    icon: Any?,
    placeholder: String? = null,
    supportIcon: Any? = null,
    size: Dp = listItemIconSize,
    supportSize: Dp = listItemSupportIconSize,
) {
    icon ?: return
    Box {
        AsyncImage(
            model = icon,
            placeholderText = placeholder,
            contentDescription = "list_item_icon",
            size = size
        )
        supportIcon?.let {
            AsyncImage(
                modifier = Modifier
                    .size(supportSize)
                    .align(Alignment.Companion.BottomEnd)
                    .border(0.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                model = supportIcon,
                contentDescription = "list_item_support_icon",
            )
        }
    }
}