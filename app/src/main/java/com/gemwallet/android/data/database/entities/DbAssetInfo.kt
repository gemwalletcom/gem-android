package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

@DatabaseView(
    viewName = "assets_info",
    value = """
        SELECT
            assets.*,
            accounts.*,
            session.currency AS priceCurrency,
            wallets.type AS walletType,
            wallets.name AS walletName,
            prices.value AS priceValue,
            prices.dayChanged AS priceDayChanges,
            balances.amount AS amount,
            balances.type as balanceType
        FROM assets
        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
        JOIN wallets ON wallets.id = accounts.wallet_id
        JOIN session ON accounts.wallet_id = session.wallet_id AND session.id == 1
        LEFT JOIN balances ON assets.owner_address = balances.address AND assets.id = balances.asset_id
        LEFT JOIN prices ON assets.id = prices.assetId
    """
)
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