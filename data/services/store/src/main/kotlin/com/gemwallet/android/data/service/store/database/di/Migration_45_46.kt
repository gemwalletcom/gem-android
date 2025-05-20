package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_45_46 : Migration(45, 46) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `extended_txs`")

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
            |            WHERE (tx.owner IN (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1) OR tx.recipient in (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1))
            |                AND tx.walletId in (SELECT wallet_id FROM session WHERE session.id = 1)
            |                AND UPPER(tx.feeAssetId) IN (SELECT UPPER(accounts.chain) FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1)
            |            GROUP BY tx.id
            """.trimMargin()
        )
    }
}