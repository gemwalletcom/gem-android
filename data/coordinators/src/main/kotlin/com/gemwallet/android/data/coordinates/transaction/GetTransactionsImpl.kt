package com.gemwallet.android.data.coordinates.transaction

import androidx.compose.runtime.Stable
import com.gemwallet.android.application.transactions.coordinators.GetTransactions
import com.gemwallet.android.data.repositoreis.transactions.TransactionRepository
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetTransactionsImpl(
    private val transactionsRepository: TransactionRepository,
) : GetTransactions {

    override fun getTransactions(
        assetId: AssetId?,
        state: TransactionState?,
        filterByChains: List<Chain>,
        filterByType: List<TransactionType>
    ): Flow<List<TransactionDataAggregate>> = transactionsRepository.getTransactions()
        .map { transactions ->
            transactions.filter { tx ->
                val byChain = if (filterByChains.isEmpty()) {
                    true
                } else {
                    val txChains = (tx.assets + listOf(tx.asset, tx.feeAsset)).map { it.chain }.toSet()
                    filterByChains.containsAll(txChains)
                }
                val byType = if (filterByType.isEmpty()) {
                    true
                } else {
                    filterByType.contains(tx.transaction.type)
                }
                byChain && byType
            }
        }
        .map { txs -> txs.filter { state == null || it.transaction.state == state } }
        .map { items ->
            items.filter {
                val swapMetadata = it.transaction.getSwapMetadata()
                assetId == null || it.asset.id == assetId
                        || swapMetadata?.toAsset == assetId
                        || swapMetadata?.fromAsset == assetId
            }
        }
        .map { items -> items.map { TransactionDataAggregateImpl(it) } }
        .flowOn(Dispatchers.IO)
}

@Stable
class TransactionDataAggregateImpl(
    private val data: TransactionExtended
) : TransactionDataAggregate {

    override val id: String = data.transaction.id

    override val asset: Asset = data.asset

    override val address: String get() = when (data.transaction.type) {
        TransactionType.TransferNFT,
        TransactionType.Transfer -> when (data.transaction.direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> data.transaction.to.getAddressEllipsisText()
            TransactionDirection.Incoming -> data.transaction.from.getAddressEllipsisText()
        }
        TransactionType.Swap,
        TransactionType.TokenApproval,
        TransactionType.StakeDelegate,
        TransactionType.StakeUndelegate,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw,
        TransactionType.AssetActivation,
        TransactionType.StakeRewards,
        TransactionType.SmartContractCall,
        TransactionType.PerpetualOpenPosition,
        TransactionType.StakeFreeze,
        TransactionType.StakeUnfreeze,
        TransactionType.PerpetualClosePosition,
        TransactionType.PerpetualModifyPosition
            -> ""
    }

    override val value: String get() = when (data.transaction.type) {
        TransactionType.Swap -> {
            getSwapMetadata(true)?.let { (metadata, asset) ->
                "+${asset.format(Crypto(metadata.toValue), decimalPlace = 2, dynamicPlace = true)}"
            } ?: ""
        }
        TransactionType.StakeUndelegate,
        TransactionType.StakeRewards,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw,
        TransactionType.StakeDelegate,
        TransactionType.PerpetualOpenPosition,
        TransactionType.StakeFreeze,
        TransactionType.StakeUnfreeze,
        TransactionType.PerpetualClosePosition -> getFormattedValue()
        TransactionType.Transfer -> {
            when (data.transaction.direction) {
                TransactionDirection.SelfTransfer,
                TransactionDirection.Outgoing -> "-${getFormattedValue()}"
                TransactionDirection.Incoming -> "+${getFormattedValue()}"
            }
        }
        TransactionType.TokenApproval,
        TransactionType.TransferNFT,
        TransactionType.AssetActivation,
        TransactionType.SmartContractCall,
        TransactionType.PerpetualModifyPosition
            -> ""
    }

    override val equivalentValue: String? get() = when (data.transaction.type) {
        TransactionType.Swap -> getSwapMetadata(false)?.let { (metadata, asset) ->
            "-${
                asset.format(
                    Crypto(metadata.fromValue),
                    decimalPlace = 2,
                    dynamicPlace = true
                )
            }"
        }
        else -> null
    }

    override val type: TransactionType = data.transaction.type

    override val direction: TransactionDirection  = data.transaction.direction

    override val state: TransactionState = data.transaction.state
    override val createdAt: Long = data.transaction.createdAt

    private fun getSwapMetadata(toAsset: Boolean): Pair<TransactionSwapMetadata, Asset>? {
        val swapMetadata = data.transaction.getSwapMetadata() ?: return null
        val asset = if (toAsset) {
            data.assets.firstOrNull { swapMetadata.toAsset == it.id }
        } else {
            data.assets.firstOrNull { swapMetadata.fromAsset == it.id }
        } ?: return null

        return Pair(swapMetadata, asset)
    }

    private fun getFormattedValue(): String = data.asset.format(
        crypto = data.transaction.value.toBigInteger(),
        decimalPlace = 2,
        dynamicPlace = true,
    )

}