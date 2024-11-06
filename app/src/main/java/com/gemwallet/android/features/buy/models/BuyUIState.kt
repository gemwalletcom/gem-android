package com.gemwallet.android.features.buy.models

import androidx.compose.runtime.Stable
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.FiatProvider

sealed interface BuyUIState {
    data class Idle(
        val isQuoteLoading: Boolean = false,
        val asset: AssetItemUIModel? = null,
        val cryptoAmount: String = "",
        val fiatAmount: String = "",
        val currentProvider: Provider? = null,
        val redirectUrl: String? = null,
        val providers: List<Provider> = emptyList(),
        val error: BuyError? = null,
    ) : BuyUIState {
        fun isAvailable(): Boolean = !isQuoteLoading && error == null && redirectUrl != null && currentProvider != null
    }

    data class Fatal(
        val message: String = "",
    ) : BuyUIState

    @Stable
    data class Provider(
        val provider: FiatProvider,
        val cryptoAmount: String,
        val rate: String,
    )
}