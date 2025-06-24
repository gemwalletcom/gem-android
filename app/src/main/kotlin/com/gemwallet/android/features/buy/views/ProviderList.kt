package com.gemwallet.android.features.buy.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.gemwallet.android.features.buy.models.BuyFiatProviderUIModel
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.PropertyDataText
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.PropertyTitleText
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
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
            items(providers) {
                PropertyItem(
                    modifier = Modifier.clickable(
                        onClick = {
                            onProviderSelect(it.provider)
                            isShow.value = false
                        }
                    ),
                    title = {
                        PropertyTitleText(
                            text = it.provider.name,
                            trailing = { AsyncImage("file:///android_asset/fiat/${it.provider.name.lowercase()}.png") }
                        )
                    },
                    data = {
                        PropertyDataText(
                            text = it.cryptoFormatted,
                            badge = { DataBadgeChevron() }
                        )
                    },
                )
            }
        }
    }
}