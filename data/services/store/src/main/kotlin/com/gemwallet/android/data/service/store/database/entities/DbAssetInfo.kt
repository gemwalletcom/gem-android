package com.gemwallet.android.data.service.store.database.entities

import androidx.room.DatabaseView
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.isStakeSupported
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.AssetPriceInfo
import com.gemwallet.android.model.Balance
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@DatabaseView(
    viewName = "asset_info",
    value = """
        SELECT DISTINCT
            asset.id as id,
            asset.name as name,
            asset.symbol as symbol,
            asset.decimals as decimals,
            asset.type as type,
            asset.is_buy_enabled as isBuyEnabled,
            asset.is_sell_enabled as isSellEnabled,
            asset.is_swap_enabled as isSwapEnabled,
            asset.is_stake_enabled as isStakeEnabled,
            asset.staking_apr as stakingApr,
            asset.rank as assetRank,
            asset.chain as chain,
            accounts.address as address,
            accounts.derivation_path as derivationPath,
            accounts.extendedPublicKey as extendedPublicKey,
            asset_config.is_pinned AS pinned,
            asset_config.is_visible AS visible,
            asset_config.list_position AS listPosition,
            session.id AS sessionId,
            prices.currency AS priceCurrency,
            wallets.id as walletId,
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
            balances.updated_at AS balanceUpdatedAt,
            balances.is_active AS assetIsActive
        FROM asset
        LEFT JOIN asset_wallet ON asset.id = asset_wallet.asset_id
        LEFT JOIN session ON asset_wallet.wallet_id = session.wallet_id
        LEFT JOIN wallets ON wallets.id = "session".wallet_id
        LEFT JOIN accounts ON asset_wallet.account_address = accounts.address AND wallets.id = accounts.wallet_id AND asset."chain" = accounts."chain"
        LEFT JOIN balances ON asset_wallet.account_address = balances.account_address AND asset_wallet.asset_id = balances.asset_id AND wallets.id = balances.wallet_id
        LEFT JOIN prices ON asset.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
        LEFT JOIN asset_config ON asset_wallet.asset_id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
    """
)
data class DbAssetInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val pinned: Boolean?,
    val visible: Boolean?,
    val listPosition: Int?,
    val isBuyEnabled: Boolean,
    val isSellEnabled: Boolean,
    val isSwapEnabled: Boolean,
    val isStakeEnabled: Boolean,
    val stakingApr: Double?,
    val assetRank: Int,
    // account
    val address: String?,
    val walletId: String?,
    val derivationPath: String?,
    val chain: Chain,
    val extendedPublicKey: String?,
    // wallet
    val sessionId: Int?,
    val walletName: String?,
    val walletType: WalletType?,
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
    val assetIsActive: Boolean?,

    val balanceUpdatedAt: Long?,
)

fun Flow<List<DbAssetInfo>>.toAssetInfoModel() = map { it.toAssetInfoModels() }

fun List<DbAssetInfo>.toAssetInfoModels() = mapNotNull { it.toModel() }

fun DbAssetInfo.toModel(): AssetInfo? {
    val entity = this
    val assetId = entity.id.toAssetId() ?: return null
    val asset = Asset(
        id = assetId,
        name = entity.name,
        symbol = entity.symbol,
        decimals = entity.decimals,
        type = entity.type,
    )
    val balances = AssetBalance(
        asset = asset,
        balance = Balance(
            available = entity.balanceAvailable ?: "0",
            frozen = entity.balanceFrozen ?: "0",
            locked = entity.balanceLocked ?: "0",
            staked = entity.balanceStaked ?: "0",
            pending = entity.balancePending ?: "0",
            rewards = entity.balanceRewards ?: "0",
            reserved = entity.balanceReserved ?: "0"
        ),
        balanceAmount = Balance(
            available = entity.balanceAvailableAmount ?: 0.0,
            frozen = entity.balanceFrozenAmount ?: 0.0,
            locked = entity.balanceLockedAmount ?: 0.0,
            staked = entity.balanceStakedAmount ?: 0.0,
            pending = entity.balancePendingAmount ?: 0.0,
            rewards = entity.balanceRewardsAmount ?: 0.0,
            reserved = entity.balanceReservedAmount ?: 0.0,
        ),
        totalAmount = entity.balanceTotalAmount ?: 0.0,
        fiatTotalAmount = entity.balanceFiatTotalAmount ?: 0.0,
        isActive = when (assetId.chain) {
            Chain.Xrp -> assetIsActive != false // TODO: Fast fix removed in price websockets. Idea: Some users have inconsistent database.
            else -> true
        },
    )

    val currency = Currency.entries.firstOrNull { it.string == entity.priceCurrency }
    val account = if (entity.address.isNullOrEmpty()) null else Account(
        chain = entity.chain,
        address = entity.address,
        derivationPath = entity.derivationPath ?: "",
        extendedPublicKey = entity.extendedPublicKey,
    )

    return AssetInfo(
        owner = account,
        asset = asset,
        balance = balances,
        price = if (entity.priceValue != null && currency != null) {
            AssetPriceInfo(
                currency = currency,
                price = AssetPrice(
                    assetId = assetId,
                    price = entity.priceValue,
                    priceChangePercentage24h = entity.priceDayChanges ?: 0.0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else null,
        metadata = AssetMetaData(
            isEnabled = entity.visible == true,
            isBuyEnabled = entity.isBuyEnabled,
            isSellEnabled = entity.isSellEnabled,
            isSwapEnabled = entity.isSwapEnabled,
            isStakeEnabled = asset.chain().isStakeSupported(),
            isPinned = entity.pinned == true,
            rankScore = entity.assetRank,
            isActive = true,
        ),
        rank = entity.assetRank,
        walletName = entity.walletName ?: "",
        walletType = entity.walletType ?: WalletType.multicoin,
        stakeApr = entity.stakingApr,
        position = entity.listPosition ?: 0,
        walletId = walletId,
    )
}