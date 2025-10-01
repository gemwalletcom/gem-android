package com.gemwallet.features.buy.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.getListPosition
import com.gemwallet.features.buy.viewmodels.models.BuyFiatProviderUIModel
import com.wallet.core.primitives.FiatProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderList(
    isShow: MutableState<Boolean>,
    providers: List<BuyFiatProviderUIModel>,
    onProviderSelect: (FiatProvider) -> Unit,
) {

    if (!isShow.value) {
        return
    }

    ModalBottomSheet(
        onDismissRequest = { isShow.value = false },
    ) {
        LazyColumn {
            itemsIndexed(providers) { index, item ->
                PropertyItem(
                    modifier = Modifier.clickable {
                        onProviderSelect(item.provider)
                        isShow.value = false
                    },
                    title = {
                        PropertyTitleText(
                            text = item.provider.name,
                            trailing = { AsyncImage("file:///android_asset/fiat/${item.provider.name.lowercase()}.png") }
                        )
                    },
                    data = {
                        PropertyDataText(
                            text = item.cryptoFormatted,
                            badge = { DataBadgeChevron() }
                        )
                    },
                    listPosition = providers.getListPosition(index),
                )
            }
        }
    }
}