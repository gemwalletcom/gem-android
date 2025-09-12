package com.gemwallet.features.swap.views.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import uniffi.gemstone.SwapperProvider

@Composable
internal fun SwapProviderItemView(
    swapProvider: SwapProviderItem,
    isSelected: Boolean,
    onProviderSelect: (SwapperProvider) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onProviderSelect(swapProvider.swapProvider.id) }),
        leading = {
            AsyncImage(model = swapProvider.icon,)
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemTitleText(swapProvider.name)
                if (isSelected) {
                    Spacer4()
                    Icon(
                        modifier = Modifier.Companion
                            .size(18.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        imageVector = Icons.Default.Check,
                        contentDescription = "selected"
                    )
                }
            }
        },
        dividerShowed = false,
        trailing = {
            swapProvider.price?.let { price ->
                Column(horizontalAlignment = Alignment.End) {
                    ListItemTitleText(price)
                    swapProvider.fiat?.let {
                        ListItemSupportText(it)
                    }

                }
            }
        }
    )
}