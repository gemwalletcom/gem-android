package com.gemwallet.android.features.buy.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.gemwallet.android.features.buy.models.BuyFiatProviderUIModel
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Table
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
        Table(
            items = providers.map {
                CellEntity(
                    icon = "file:///android_asset/fiat/${it.provider.name.lowercase()}.png",
                    label = it.provider.name,
                    data = it.cryptoFormatted
                ) {
                    onProviderSelect(it.provider)
                    isShow.value = false
                }
            }
        )
    }
}