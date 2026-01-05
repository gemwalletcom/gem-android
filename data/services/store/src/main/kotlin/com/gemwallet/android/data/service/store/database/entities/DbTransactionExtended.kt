package com.gemwallet.android.data.service.store.database.entities

import androidx.room.DatabaseView
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.model.Transaction
import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Price
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

const val SESSION_REQUEST = """SELECT accounts.address FROM accounts, session
    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1"""
const val SESSION_CHAINS_REQUEST = """SELECT UPPER(accounts.chain) FROM accounts, session
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
            asset.decimals as assetDecimals,
            asset.name as assetName,
            asset.type as assetType,
            asset.symbol as assetSymbol,
            feeAsset.decimals as feeDecimals,
            feeAsset.name as feeName,
            feeAsset.type as feeType,
            feeAsset.symbol as feeSymbol,
            prices.value as assetPrice,
            prices.day_changed as assetPriceChanged,
            feePrices.value as feePrice,
            feePrices.day_changed as feePriceChanged,
            from_asset.id as assetIdFrom,
            from_asset.name as assetNameFrom,
            from_asset.symbol as assetSymbolFrom,
            from_asset.decimals as assetDecimalsFrom,
            from_asset.type as assetTypeFrom,
            to_asset.id as assetIdTo,
            to_asset.name as assetNameTo,
            to_asset.symbol as assetSymbolTo,
            to_asset.decimals as assetDecimalsTo,
            to_asset.type as assetTypeTo
        FROM transactions as tx
            INNER JOIN asset ON tx.assetId = asset.id 
            INNER JOIN asset as feeAsset ON tx.feeAssetId = feeAsset.id
            LEFT JOIN prices ON tx.assetId = prices.asset_id
            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.asset_id
            LEFT JOIN tx_swap_metadata as swap ON tx.id = swap.tx_id
            LEFT JOIN asset as from_asset ON swap.from_asset_id = from_asset.id
            LEFT JOIN asset as to_asset ON swap.to_asset_id = to_asset.id
            WHERE (tx.owner IN ($SESSION_REQUEST) OR tx.recipient in ($SESSION_REQUEST))
                AND tx.walletId in ($CURRENT_WALLET_REQUEST)
                AND UPPER(tx.feeAssetId) IN ($SESSION_CHAINS_REQUEST)
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
    val assetIdFrom: String?,
    val assetNameFrom: String?,
    val assetSymbolFrom: String?,
    val assetDecimalsFrom: Int?,
    val assetTypeFrom: AssetType?,
    val assetIdTo: String?,
    val assetNameTo: String?,
    val assetSymbolTo: String?,
    val assetDecimalsTo: Int?,
    val assetTypeTo: AssetType?,
)

fun DbTransactionExtended.toDTO(): TransactionExtended? {
    return TransactionExtended(
        transaction = Transaction(
            id = this.id,
            assetId = this.assetId.toAssetId() ?: return null,
            from = this.owner,
            to = this.recipient,
            contract = this.contract,
            type = this.type,
            state = this.state,
            blockNumber = this.blockNumber,
            sequence = this.sequence,
            fee = this.fee,
            feeAssetId = this.feeAssetId.toAssetId() ?: return null,
            value = this.value,
            memo = this.payload,
            direction = this.direction,
            utxoInputs = emptyList(),
            utxoOutputs = emptyList(),
            createdAt = this.createdAt,
            metadata = this.metadata,
        ),
        asset = Asset(
            id = this.assetId.toAssetId() ?: return null,
            name = this.assetName,
            symbol = this.assetSymbol,
            decimals = this.assetDecimals,
            type = this.assetType,
        ),
        feeAsset = Asset(
            id = this.feeAssetId.toAssetId() ?: return null,
            name = this.feeName,
            symbol = this.feeSymbol,
            decimals = this.feeDecimals,
            type = this.feeType,
        ),
        price = if (this.assetPrice == null)
            null
        else
            Price(this.assetPrice, this.assetPriceChanged ?: 0.0, 0L),
        feePrice = if (this.feePrice == null)
            null
        else
            Price(this.feePrice, this.feePriceChanged ?: 0.0, 0L),
        assets = listOfNotNull(
            assetIdFrom?.toAssetId()?.let {
                Asset(
                    id = it,
                    name = assetNameFrom ?: "",
                    decimals = assetDecimalsFrom ?: 0,
                    symbol = assetSymbolFrom ?: "",
                    type = assetTypeFrom ?: AssetType.NATIVE,
                )
            },
            assetIdTo?.toAssetId()?.let {
                Asset(
                    id = it,
                    name = assetNameTo ?: "",
                    decimals = assetDecimalsTo ?: 0,
                    symbol = assetSymbolTo ?: "",
                    type = assetTypeTo ?: AssetType.NATIVE,
                )
            }
        ),
    )
}

fun List<DbTransactionExtended>.toDTO() = mapNotNull { it.toDTO() }