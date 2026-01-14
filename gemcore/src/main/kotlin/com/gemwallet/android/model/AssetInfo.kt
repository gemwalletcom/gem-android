package com.gemwallet.android.model

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.WalletType

data class AssetInfo(
    val owner: Account?,
    val asset: Asset,
    val balance: AssetBalance = AssetBalance(asset),
    val walletId: String?,
    val walletType: WalletType = WalletType.View,
    val walletName: String = "",
    val price: AssetPriceInfo? = null,
    val metadata: AssetMetaData? = null,
    val rank: Int = 0,
    val stakeApr: Double? = null,
    val position: Int = 0,
) {
    fun id() = asset.id

    override fun equals(other: Any?): Boolean {
        return (other as? AssetInfo)?.let { info ->
            asset == info.asset
                    && metadata?.isActive == info.metadata?.isActive
                    && metadata?.isSwapEnabled == info.metadata?.isSwapEnabled
                    && metadata?.isBalanceEnabled == info.metadata?.isBalanceEnabled
                    && metadata?.isBuyEnabled == info.metadata?.isBuyEnabled
                    && metadata?.isEnabled == info.metadata?.isEnabled
                    && metadata?.isPinned == info.metadata?.isPinned
                    && metadata?.isSellEnabled == info.metadata?.isSellEnabled
                    && metadata?.isStakeEnabled == info.metadata?.isStakeEnabled
                    && metadata?.isSwapEnabled == info.metadata?.isSwapEnabled
                    && metadata?.rankScore == info.metadata?.rankScore
                    && metadata?.stakingApr == info.metadata?.stakingApr
                    && rank == info.rank
                    && position == info.position
                    && price == info.price
                    && walletName == info.walletName
                    && walletType == info.walletType
                    && walletId == info.walletId
                    && balance == info.balance
                    && owner == info.owner
        } == true
    }

    override fun hashCode(): Int {
        var result = owner.hashCode()
        result = 31 * result + asset.hashCode()
        result = 31 * result + this@AssetInfo.balance.hashCode()
        result = 31 * result + walletId.hashCode()
        result = 31 * result + walletType.hashCode()
        result = 31 * result + walletName.hashCode()
        result = 31 * result + (price?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isActive?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isBalanceEnabled?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isBuyEnabled?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isEnabled?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isPinned?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isSellEnabled?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isStakeEnabled?.hashCode() ?: 0)
        result = 31 * result + (metadata?.isSwapEnabled?.hashCode() ?: 0)
        result = 31 * result + (metadata?.rankScore ?: 0)
        result = 31 * result + (metadata?.stakingApr?.hashCode() ?: 0)
        result = 31 * result + rank
        result = 31 * result + (stakeApr?.hashCode() ?: 0)
        result = 31 * result + position
        return result
    }

    companion object
}
