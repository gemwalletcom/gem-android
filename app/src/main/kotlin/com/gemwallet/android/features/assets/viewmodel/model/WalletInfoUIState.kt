package com.gemwallet.android.features.assets.viewmodel.model

import com.gemwallet.android.ui.models.PriceState
import com.wallet.core.primitives.WalletType

data class WalletInfoUIState(
    val name: String = "",
    val icon: Any? = null,
    val totalValue: String = "0.0",
    val changedValue: String = "0.0",
    val changedPercentages: String = "0.0%",
    val priceState: PriceState = PriceState.Up,
    val type: WalletType = WalletType.view,
)