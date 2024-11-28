package com.gemwallet.android.features.asset.details.models

import com.gemwallet.android.ui.models.PriceState
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.WalletType

class AssetInfoUIModel(
    val asset: Asset,
    val name: String = "",
    val iconUrl: String = "",
    val priceValue: String = "0",
    val priceDayChanges: String = "0",
    val priceChangedType: PriceState = PriceState.Up,
    val tokenType: AssetType = AssetType.NATIVE,
    val networkTitle: String = "",
    val account: Account = Account(),
    val isBuyEnabled: Boolean = false,
    val isSwapEnabled: Boolean = false,
    val updated: Long = System.currentTimeMillis(),
) {
    data class Account(
        val walletType: WalletType = WalletType.view,
        val totalBalance: String = "0",
        val totalFiat: String = "0",
        val owner: String = "",
        val hasBalanceDetails: Boolean = false,
        val available: String = "0",
        val stake: String = "0",
        val reserved: String = "0",
    )
}