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

@Composable
fun IconWithSupport(
    icon: Any,
    placeholder: String? = null,
    supportIcon: Any? = null,
    size: Dp = listItemIconSize,
) {
    Box {
        AsyncImage(
            model = icon,
            placeholderText = placeholder,
            contentDescription = "list_item_icon",
            size = size
        )
        if (supportIcon != null) {
            AsyncImage(
                modifier = Modifier.Companion
                    .size(18.dp)
                    .align(Alignment.Companion.BottomEnd)
                    .border(0.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                model = supportIcon,
                contentDescription = "list_item_support_icon",
            )
        }
    }
}