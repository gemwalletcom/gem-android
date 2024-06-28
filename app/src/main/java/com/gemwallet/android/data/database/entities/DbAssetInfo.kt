package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

data class DbAssetInfo(
    @ColumnInfo("owner_address", index = true) val address: String,
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    @ColumnInfo("is_visible") val isVisible: Boolean = true,
    @ColumnInfo("is_buy_enabled") val isBuyEnabled: Boolean = false,
    @ColumnInfo("is_swap_enabled") val isSwapEnabled: Boolean = false,
    @ColumnInfo("is_stake_enabled") val isStakeEnabled: Boolean = false,
    @ColumnInfo("staking_apr") val stakingApr: Double? = null,
    @ColumnInfo("links") val links: String? = null,
    @ColumnInfo("market") val market: String? = null,
    @ColumnInfo("rank") val rank: Int = 0,
    // account
    @ColumnInfo(name = "wallet_id") val walletId: String,
    @ColumnInfo(name = "derivation_path") val derivationPath: String,
    val chain: Chain,
    val extendedPublicKey: String?,
    // wallet
    val walletName: String,
    val walletType: WalletType,
    // price
    val priceValue: Double?,
    val priceDayChanges: Double?,
    val priceCurrency: String?,
    // balance
    val balanceType: BalanceType?,
    val amount: String?,
)