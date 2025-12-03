package com.gemwallet.features.assets.viewmodels.model

import com.gemwallet.android.ui.models.PriceState
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.WalletType
import java.math.BigDecimal

data class WalletInfoUIState(
    val name: String = "",
    val icon: Any? = null,
    val cryptoTotalValue: Double = 0.0,
    val totalValueFormatted: String = "0.0",
    val changedValue: String = "0.0",
    val changedPercentages: String = "0.0%",
    val priceState: PriceState = PriceState.Up,
    val type: WalletType = WalletType.view,
    val operationsEnabled: Boolean = true,
    val isSwapEnabled: Boolean = false,
    val swapPayAsset: Asset? = null
)