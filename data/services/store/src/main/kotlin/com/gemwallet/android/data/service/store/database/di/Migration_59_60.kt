package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_59_60 : Migration(59, 60) {
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
            |            feePrices.day_changed as feePriceChanged,
            |            from_asset.id as assetIdFrom,
            |            from_asset.name as assetNameFrom,
            |            from_asset.symbol as assetSymbolFrom,
            |            from_asset.decimals as assetDecimalsFrom,
            |            from_asset.type as assetTypeFrom,
            |            to_asset.id as assetIdTo,
            |            to_asset.name as assetNameTo,
            |            to_asset.symbol as assetSymbolTo,
            |            to_asset.decimals as assetDecimalsTo,
            |            to_asset.type as assetTypeTo
            |        FROM transactions as tx
            |            INNER JOIN asset ON tx.assetId = asset.id 
            |            INNER JOIN asset as feeAsset ON tx.feeAssetId = feeAsset.id
            |            LEFT JOIN prices ON tx.assetId = prices.asset_id
            |            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.asset_id
            |            LEFT JOIN tx_swap_metadata as swap ON tx.id = swap.tx_id
            |            LEFT JOIN asset as from_asset ON swap.from_asset_id = from_asset.id
            |            LEFT JOIN asset as to_asset ON swap.to_asset_id = to_asset.id
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