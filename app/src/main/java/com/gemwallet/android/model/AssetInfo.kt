package com.gemwallet.android.model

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.WalletType

data class AssetInfo(
    val owner: Account,
    val asset: Asset,
    val balances: Balances = Balances(),
    val walletType: WalletType = WalletType.view,
    val walletName: String = "",
    val price: AssetPriceInfo? = null,
    val metadata: AssetMetaData? = null,
    val links: AssetLinks? = null,
    val market: AssetMarket? = null,
    val rank: Int = 0,
    val stakeApr: Double? = null,
    val position: Int = 0,
) {
    fun id() = asset.id
}
