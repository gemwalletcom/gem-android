package com.gemwallet.android.data.service.store.database.entities

import androidx.room.DatabaseView
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

@DatabaseView(
    viewName = "asset_info",
    value = """
        SELECT
        assets.owner_address as address,
        assets.id as id,
        assets.name as name,
        assets.symbol as symbol,
        assets.decimals as decimals,
        assets.type as type,
        assets.is_buy_enabled as isBuyEnabled,
        assets.is_swap_enabled as isSwapEnabled,
        assets.is_stake_enabled as isStakeEnabled,
        assets.staking_apr as stakingApr,
        assets.links as links,
        assets.market as market,
        assets.rank as assetRank,
        accounts.derivation_path as derivationPath,
        accounts.chain as chain,
        accounts.wallet_id as walletId,
        accounts.extendedPublicKey as extendedPublicKey,
        asset_config.is_pinned AS pinned,
        asset_config.is_visible AS visible,
        asset_config.list_position AS listPosition,
        session.id AS sessionId,
        prices.currency AS priceCurrency,
        wallets.type AS walletType,
        wallets.name AS walletName,
        prices.value AS priceValue,
        prices.day_changed AS priceDayChanges,
        balances.available AS balanceAvailable,
        balances.available_amount AS balanceAvailableAmount,
        balances.frozen AS balanceFrozen,
        balances.frozen_amount AS balanceFrozenAmount,
        balances.locked AS balanceLocked,
        balances.locked_amount AS balanceLockedAmount,
        balances.staked AS balanceStaked,
        balances.staked_amount AS balanceStakedAmount,
        balances.pending AS balancePending,
        balances.pending_amount AS balancePendingAmount,
        balances.rewards AS balanceRewards,
        balances.rewards_amount AS balanceRewardsAmount,
        balances.reserved AS balanceReserved,
        balances.reserved_amount AS balanceReservedAmount,
        balances.total_amount AS balanceTotalAmount,
        (balances.total_amount * prices.value) AS balanceFiatTotalAmount,
        balances.updated_at AS balanceUpdatedAt
        FROM assets
        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
        JOIN wallets ON wallets.id = accounts.wallet_id
        LEFT JOIN session ON accounts.wallet_id = session.wallet_id
        LEFT JOIN balances ON assets.owner_address = balances.owner AND assets.id = balances.asset_id
        LEFT JOIN prices ON assets.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
        LEFT JOIN asset_config ON assets.id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
    """
)
data class DbAssetInfo(
    val address: String,
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val pinned: Boolean?,
    val visible: Boolean?,
    val listPosition: Int?,
    val isBuyEnabled: Boolean,
    val isSwapEnabled: Boolean,
    val isStakeEnabled: Boolean,
    val stakingApr: Double?,
    val links: String?,
    val market: String?,
    val assetRank: Int,
    // account
    val walletId: String,
    val derivationPath: String,
    val chain: Chain,
    val extendedPublicKey: String?,
    // wallet
    val sessionId: Int?,
    val walletName: String,
    val walletType: WalletType,
    // price
    val priceValue: Double?,
    val priceDayChanges: Double?,
    val priceCurrency: String?,
    // balance
    val balanceAvailable: String?,
    val balanceAvailableAmount: Double?,
    val balanceFrozen: String?,
    val balanceFrozenAmount: Double?,
    val balanceLocked: String?,
    val balanceLockedAmount: Double?,
    val balanceStaked: String?,
    val balanceStakedAmount: Double?,
    val balancePending: String?,
    val balancePendingAmount: Double?,
    val balanceRewards: String?,
    val balanceRewardsAmount: Double?,
    val balanceReserved: String?,
    val balanceReservedAmount: Double?,
    val balanceTotalAmount: Double?,
    val balanceFiatTotalAmount: Double?,

    val balanceUpdatedAt: Long?,
)