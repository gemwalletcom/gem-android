package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_46_47 : Migration(46, 47) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")
        db.execSQL("ALTER TABLE asset ADD COLUMN is_sell_enabled INTEGER NOT NULL DEFAULT FALSE")

        db.execSQL(
            """
            |CREATE VIEW `asset_info` AS SELECT DISTINCT
            |            asset.id as id,
            |            asset.name as name,
            |            asset.symbol as symbol,
            |            asset.decimals as decimals,
            |            asset.type as type,
            |            asset.is_buy_enabled as isBuyEnabled,
            |            asset.is_sell_enabled as isSellEnabled,
            |            asset.is_swap_enabled as isSwapEnabled,
            |            asset.is_stake_enabled as isStakeEnabled,
            |            asset.staking_apr as stakingApr,
            |            asset.rank as assetRank,
            |            asset.chain as chain,
            |            accounts.address as address,
            |            accounts.derivation_path as derivationPath,
            |            accounts.extendedPublicKey as extendedPublicKey,
            |            asset_config.is_pinned AS pinned,
            |            asset_config.is_visible AS visible,
            |            asset_config.list_position AS listPosition,
            |            session.id AS sessionId,
            |            prices.currency AS priceCurrency,
            |            wallets.id as walletId,
            |            wallets.type AS walletType,
            |            wallets.name AS walletName,
            |            prices.value AS priceValue,
            |            prices.day_changed AS priceDayChanges,
            |            balances.available AS balanceAvailable,
            |            balances.available_amount AS balanceAvailableAmount,
            |            balances.frozen AS balanceFrozen,
            |            balances.frozen_amount AS balanceFrozenAmount,
            |            balances.locked AS balanceLocked,
            |            balances.locked_amount AS balanceLockedAmount,
            |            balances.staked AS balanceStaked,
            |            balances.staked_amount AS balanceStakedAmount,
            |            balances.pending AS balancePending,
            |            balances.pending_amount AS balancePendingAmount,
            |            balances.rewards AS balanceRewards,
            |            balances.rewards_amount AS balanceRewardsAmount,
            |            balances.reserved AS balanceReserved,
            |            balances.reserved_amount AS balanceReservedAmount,
            |            balances.total_amount AS balanceTotalAmount,
            |            (balances.total_amount * prices.value) AS balanceFiatTotalAmount,
            |            balances.updated_at AS balanceUpdatedAt
            |        FROM asset
            |        LEFT JOIN asset_wallet ON asset.id = asset_wallet.asset_id
            |        LEFT JOIN session ON asset_wallet.wallet_id = session.wallet_id
            |        LEFT JOIN wallets ON wallets.id = "session".wallet_id
            |        LEFT JOIN accounts ON asset_wallet.account_address = accounts.address AND wallets.id = accounts.wallet_id AND asset."chain" = accounts."chain"
            |        LEFT JOIN balances ON asset_wallet.account_address = balances.account_address AND asset_wallet.asset_id = balances.asset_id AND wallets.id = balances.wallet_id
            |        LEFT JOIN prices ON asset.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON asset_wallet.asset_id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
            """.trimMargin()
        )
    }
}