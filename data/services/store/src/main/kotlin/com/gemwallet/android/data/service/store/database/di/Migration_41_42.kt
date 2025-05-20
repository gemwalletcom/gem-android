package com.gemwallet.android.data.service.store.database.di

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_41_42 : Migration(41, 42) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE asset (
                	id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    symbol TEXT NOT NULL,
                    decimals INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    chain TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL,
                    is_buy_enabled INTEGER NOT NULL,
                    is_swap_enabled INTEGER NOT NULL,
                    is_stake_enabled INTEGER NOT NULL,
                    staking_apr REAL,
                   	rank INTEGER NOT NULL,
                   	updated_at INTEGER NOT NULL,
                   	PRIMARY KEY (id)
                )
                ;
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE asset_wallet (
                	asset_id TEXT NOT NULL,
                	wallet_id TEXT NOT NULL,
                	account_address TEXT NOT NULL,
                	PRIMARY KEY (asset_id, wallet_id, account_address),
                	FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE,
                	FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
                )
                ;
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE asset_links (
                    asset_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    PRIMARY KEY (asset_id, name),
                    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE
                )
                ;
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE asset_market (
                    asset_id TEXT NOT NULL,
                    marketCap REAL,
                    marketCapFdv REAL,
                    marketCapRank INTEGER,
                    totalVolume REAL,
                    circulatingSupply REAL,
                    totalSupply REAL,
                    maxSupply REAL,
                    PRIMARY KEY(asset_id),
                    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE
                )
                ;
            """.trimIndent()
        )
//        db.execSQL(
//            """
//                INSERT INTO asset (
//                    id,
//                    name,
//                    symbol,
//                    decimals,
//                    type,
//                    chain,
//                    is_buy_enabled,
//                    is_swap_enabled,
//                    is_stake_enabled,
//                    staking_apr,
//                    rank
//                ) VALUES SELECT
//                    id AS id,
//                    name AS name,
//                    symbol AS symbol,
//                    decimals AS decimals,
//                    type AS type,
//                    chain AS chain,
//                    is_buy_enabled AS is_buy_enabled,
//                    is_swap_enabled AS is_swap_enabled,
//                    is_stake_enabled AS is_stake_enabled,
//                    staking_apr AS staking_apr,
//                    "rank" AS "rank"
//                FROM assets GROUP BY id
//                ;
//            """.trimIndent()
//        )
        val assetsCursor = db.query("""
            SELECT
             id AS id,
             name AS name,
             symbol AS symbol,
             decimals AS decimals,
             type AS type,
             chain AS chain,
             is_buy_enabled AS is_buy_enabled,
             is_swap_enabled AS is_swap_enabled,
             is_stake_enabled AS is_stake_enabled,
             staking_apr AS staking_apr,
             "rank" AS "rank"
            FROM assets GROUP BY id
        """.trimIndent())
        while (assetsCursor.moveToNext()) {
            val values = ContentValues()
            for (colIndex in 0 ..< assetsCursor.columnCount) {
                val colName = assetsCursor.getColumnName(colIndex)
                val type = assetsCursor.getType(colIndex)
                when (type) {
                    Cursor.FIELD_TYPE_BLOB -> values.put(colName, assetsCursor.getBlob(colIndex))
                    Cursor.FIELD_TYPE_NULL -> values.putNull(colName)
                    Cursor.FIELD_TYPE_FLOAT -> values.put(colName, assetsCursor.getFloat(colIndex))
                    Cursor.FIELD_TYPE_STRING -> values.put(colName, assetsCursor.getString(colIndex))
                    Cursor.FIELD_TYPE_INTEGER -> values.put(colName, assetsCursor.getString(colIndex))
                    else -> break
                }

            }
            values.put("is_enabled", 1)
            values.put("updated_at", System.currentTimeMillis())
            db.insert("asset", SQLiteDatabase.CONFLICT_REPLACE, values)
        }
        db.execSQL(
            """
                INSERT INTO asset_wallet (
                	asset_id,
                	wallet_id,
                	account_address
                )
                SELECT DISTINCT
                	assets.id asset_id,
                	wallets.id wallet_id,
                	accounts.address account_address
                FROM
                assets
                JOIN accounts  ON assets.owner_address = accounts.address
                JOIN wallets ON accounts.wallet_id = wallets.id
                ;
            """.trimIndent()
        )
        db.execSQL("DROP TABLE IF EXISTS balances;")
        db.execSQL("""
            CREATE TABLE balances (
                asset_id TEXT NOT NULL,
                wallet_id TEXT NOT NULL,
                account_address TEXT NOT NULL,
                available TEXT NOT NULL,
                available_amount REAL NOT NULL,
                frozen TEXT NOT NULL,
                frozen_amount REAL NOT NULL,
                locked TEXT NOT NULL,
                locked_amount REAL NOT NULL,
                staked TEXT NOT NULL,
                staked_amount REAL NOT NULL,
                pending TEXT NOT NULL,
                pending_amount REAL NOT NULL,
                rewards TEXT NOT NULL,
                rewards_amount REAL NOT NULL,
                reserved TEXT NOT NULL,
                reserved_amount REAL NOT NULL,
                total_amount REAL NOT NULL,
                is_active INTEGER NOT NULL,
                updated_at  INTEGER,
                PRIMARY KEY (asset_id, wallet_id, account_address),
                FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE,
                FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("DROP VIEW IF EXISTS `extended_txs`")
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")
        db.execSQL("DROP TABLE IF EXISTS assets")
        db.execSQL("DROP TABLE IF EXISTS `tokens`")

        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |            asset.id as id,
            |            asset.name as name,
            |            asset.symbol as symbol,
            |            asset.decimals as decimals,
            |            asset.type as type,
            |            asset.is_buy_enabled as isBuyEnabled,
            |            asset.is_swap_enabled as isSwapEnabled,
            |            asset.is_stake_enabled as isStakeEnabled,
            |            asset.staking_apr as stakingApr,
            |            asset.rank as assetRank,
            |            accounts.address as address,
            |            accounts.derivation_path as derivationPath,
            |            accounts.chain as chain,
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
            |        LEFT JOIN accounts ON asset_wallet.account_address = accounts.address AND asset_wallet.wallet_id = accounts.wallet_id AND asset."chain" = accounts."chain"
            |        LEFT JOIN wallets ON asset_wallet.wallet_id = wallets.id
            |        LEFT JOIN session ON asset_wallet.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON asset_wallet.account_address = balances.account_address AND asset_wallet.asset_id = balances.asset_id AND asset_wallet.asset_id = balances.wallet_id
            |        LEFT JOIN prices ON asset.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON asset_wallet.asset_id = asset_config.asset_id AND asset_wallet.wallet_id = asset_config.wallet_id
            """.trimMargin())
        db.execSQL(
            """
            |CREATE VIEW `extended_txs` AS SELECT
            |            DISTINCT tx.id,
            |            tx.hash,
            |            tx.assetId,
            |            tx.feeAssetId,
            |            tx.owner,
            |            tx.recipient,
            |            tx.contract,
            |            tx.state,
            |            tx.type,
            |            tx.blockNumber,
            |            tx.sequence,
            |            tx.fee,
            |            tx.value,
            |            tx.payload,
            |            tx.metadata,
            |            tx.direction,
            |            tx.createdAt,
            |            tx.updatedAt,
            |            tx.walletId,
            |            asset.decimals as assetDecimals,
            |            asset.name as assetName,
            |            asset.type as assetType,
            |            asset.symbol as assetSymbol,
            |            feeAsset.decimals as feeDecimals,
            |            feeAsset.name as feeName,
            |            feeAsset.type as feeType,
            |            feeAsset.symbol as feeSymbol,
            |            prices.value as assetPrice,
            |            prices.day_changed as assetPriceChanged,
            |            feePrices.value as feePrice,
            |            feePrices.day_changed as feePriceChanged
            |        FROM transactions as tx 
            |            INNER JOIN asset ON tx.assetId = asset.id 
            |            INNER JOIN asset as feeAsset ON tx.feeAssetId = feeAsset.id 
            |            LEFT JOIN prices ON tx.assetId = prices.asset_id
            |            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.asset_id 
            |            WHERE tx.owner IN (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1) OR tx.recipient in (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1)
            |                AND tx.walletId in (SELECT wallet_id FROM session WHERE session.id = 1)
            |            GROUP BY tx.id
            """.trimMargin()
        )
    }
}

object Migration_42_43 : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")

        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |            asset.id as id,
            |            asset.name as name,
            |            asset.symbol as symbol,
            |            asset.decimals as decimals,
            |            asset.type as type,
            |            asset.is_buy_enabled as isBuyEnabled,
            |            asset.is_swap_enabled as isSwapEnabled,
            |            asset.is_stake_enabled as isStakeEnabled,
            |            asset.staking_apr as stakingApr,
            |            asset.rank as assetRank,
            |            accounts.address as address,
            |            accounts.derivation_path as derivationPath,
            |            accounts.chain as chain,
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
            |        LEFT JOIN accounts ON asset_wallet.account_address = accounts.address AND asset_wallet.wallet_id = accounts.wallet_id AND asset."chain" = accounts."chain"
            |        LEFT JOIN wallets ON asset_wallet.wallet_id = wallets.id
            |        LEFT JOIN session ON asset_wallet.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON asset_wallet.account_address = balances.account_address AND asset_wallet.asset_id = balances.asset_id AND asset_wallet.wallet_id = balances.wallet_id
            |        LEFT JOIN prices ON asset.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON asset_wallet.asset_id = asset_config.asset_id AND asset_wallet.wallet_id = asset_config.wallet_id
            """.trimMargin())
    }
}

object Migration_43_44 : Migration(43, 44) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")

        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |            asset.id as id,
            |            asset.name as name,
            |            asset.symbol as symbol,
            |            asset.decimals as decimals,
            |            asset.type as type,
            |            asset.is_buy_enabled as isBuyEnabled,
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
            |        LEFT JOIN accounts ON asset_wallet.account_address = accounts.address AND asset_wallet.wallet_id = accounts.wallet_id AND asset."chain" = accounts."chain"
            |        LEFT JOIN wallets ON asset_wallet.wallet_id = wallets.id
            |        LEFT JOIN session ON asset_wallet.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON asset_wallet.account_address = balances.account_address AND asset_wallet.asset_id = balances.asset_id AND asset_wallet.wallet_id = balances.wallet_id
            |        LEFT JOIN prices ON asset.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON asset_wallet.asset_id = asset_config.asset_id AND asset_wallet.wallet_id = asset_config.wallet_id
            """.trimMargin())
    }
}