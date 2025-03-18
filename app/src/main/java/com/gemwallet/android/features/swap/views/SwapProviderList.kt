package com.gemwallet.android.features.swap.views

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.features.swap.models.SwapProviderItem
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import uniffi.gemstone.SwapProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProviderList(
    isShow: MutableState<Boolean>,
    currentProvider: SwapProvider?,
    providers: List<SwapProviderItem>,
    onProviderSelect: (SwapProvider) -> Unit,
) {
    if (!isShow.value) {
        return
    }

    ModalBottomSheet(onDismissRequest = { isShow.value = false }, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        LazyColumn {
            items(providers) { provider ->
                SwapProviderItemView(
                    provider,
                    provider.swapProvider.id == currentProvider,
                    {
                        isShow.value = false
                        onProviderSelect(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun SwapProviderItemView(
    swapProvider: SwapProviderItem,
    isSelected: Boolean,
    onProviderSelect: (SwapProvider) -> Unit
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
                        modifier = Modifier
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