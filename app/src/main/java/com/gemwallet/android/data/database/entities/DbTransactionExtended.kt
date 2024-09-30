package com.gemwallet.android.data.database.entities

import androidx.room.DatabaseView
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

const val SESSION_REQUEST = """SELECT accounts.address FROM accounts, session
    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1"""
const val CURRENT_WALLET_REQUEST = """SELECT wallet_id FROM session WHERE session.id = 1"""

@DatabaseView(
    viewName = "extended_txs",
    value = """
        SELECT
            DISTINCT tx.id,
            tx.hash,
            tx.assetId,
            tx.feeAssetId,
            tx.owner,
            tx.recipient,
            tx.contract,
            tx.state,
            tx.type,
            tx.blockNumber,
            tx.sequence,
            tx.fee,
            tx.value,
            tx.payload,
            tx.metadata,
            tx.direction,
            tx.createdAt,
            tx.updatedAt,
            tx.walletId,
            assets.decimals as assetDecimals,
            assets.name as assetName,
            assets.type as assetType,
            assets.symbol as assetSymbol,
            feeAsset.decimals as feeDecimals,
            feeAsset.name as feeName,
            feeAsset.type as feeType,
            feeAsset.symbol as feeSymbol,
            prices.value as assetPrice,
            prices.dayChanged as assetPriceChanged,
            feePrices.value as feePrice,
            feePrices.dayChanged as feePriceChanged
        FROM transactions as tx 
            INNER JOIN assets ON tx.assetId = assets.id 
            INNER JOIN assets as feeAsset ON tx.feeAssetId = feeAsset.id 
            LEFT JOIN prices ON tx.assetId = prices.assetId
            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.assetId 
            WHERE tx.owner IN ($SESSION_REQUEST) OR tx.recipient in ($SESSION_REQUEST)
                AND tx.walletId in ($CURRENT_WALLET_REQUEST)
            GROUP BY tx.id
    """
)
data class DbTransactionExtended(
    val id: String,
    val hash: String,
    val walletId: String,
    val assetId: String,
    val feeAssetId: String,
    val owner: String,
    val recipient: String,
    val contract: String? = null,
    val state: TransactionState,
    val type: TransactionType,
    val blockNumber: String,
    val sequence: String,
    val fee: String, // Atomic value - BigInteger
    val value: String, // Atomic value - BigInteger
    val payload: String? = null,
    val metadata: String? = null,
    val direction: TransactionDirection,
    val createdAt: Long,
    val updatedAt: Long,
    val assetName: String,
    val assetSymbol: String,
    val assetDecimals: Int,
    val assetType: AssetType,
    val feeName: String,
    val feeSymbol: String,
    val feeDecimals: Int,
    val feeType: AssetType,
    val assetPrice: Double?,
    val assetPriceChanged: Double?,
    val feePrice: Double?,
    val feePriceChanged: Double?,
)