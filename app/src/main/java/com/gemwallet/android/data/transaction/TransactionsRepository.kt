package com.gemwallet.android.data.transaction

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.data.asset.AssetsLocalSource
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.Gemstone.Config
import java.math.BigInteger

class TransactionsRepository(
    private val localSource: TransactionsLocalSource,
    private val stateClients: List<TransactionStatusClient>,
    private val assetsLocalSource: AssetsLocalSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    init {
        scope.launch {
            while (true) {
                observePending()
                delay(10 * DateUtils.SECOND_IN_MILLIS)
            }
        }
    }

    fun getPendingTransactions(): Flow<List<TransactionExtended>> {
        return localSource.getTransactionsByState(TransactionState.Pending)
    }

    fun getTransactions(assetId: AssetId? = null): Flow<List<TransactionExtended>> {
        return localSource.getExtendedTransactions()
            .map { list ->
                list.filter {
                    (assetId == null || it.asset.id.toIdentifier() == assetId.toIdentifier())
                }.map {
                    val metadata = it.transaction.getSwapMetadata()
                    if (metadata != null) {
                        it.copy(
                            assets = listOf(
                                assetsLocalSource.getById(metadata.fromAsset),
                                assetsLocalSource.getById(metadata.toAsset),
                            ).mapNotNull { asset -> asset }
                        )
                    } else {
                        it
                    }
                }
            }
    }

    fun getTransaction(txId: String): Flow<TransactionExtended?> {
        return localSource.getExtendedTransaction(txId).flowOn(Dispatchers.IO)
    }

    suspend fun putTransactions(transactions: List<Transaction>) = withContext(Dispatchers.IO) {
        localSource.putTransactions(transactions)
    }

    suspend fun addTransaction(
        hash: String,
        assetId: AssetId,
        owner: Account,
        to: String,
        state: TransactionState,
        fee: Fee,
        amount: BigInteger,
        memo: String?,
        type: TransactionType,
        metadata: String? = null,
        direction: TransactionDirection,
    ): Result<Transaction> = withContext(Dispatchers.IO) {
        val transaction = Transaction(
            id = "${assetId.chain.string}_$hash",
            hash = hash,
            assetId = assetId,
            feeAssetId = fee.feeAssetId,
            from = owner.address,
            to = to,
            type = type,
            state = state,
            blockNumber = "",
            sequence = "", // Nonce
            fee = fee.amount.toString(),
            value = amount.toString(),
            memo = if (type == TransactionType.Swap) "" else memo,
            direction = direction,
            metadata = metadata,
            utxoInputs = emptyList(),
            utxoOutputs = emptyList(),
            createdAt = System.currentTimeMillis(),
        )
        localSource.addTransaction(transaction)
        Result.success(transaction)
    }

    private suspend fun observePending() = scope.launch {
        val transactions: List<Transaction> = localSource.getPending()
        val updatedTxs = transactions.map { tx ->
            async {
                val newTx = checkTx(tx)
                if (newTx != null && newTx.id != tx.id) {
                    localSource.remove(tx)
                }
                newTx
            }
        }
        .awaitAll()
        .filterNotNull()
        if (updatedTxs.isNotEmpty()) {
            localSource.updateTransaction(updatedTxs)
        }
        localSource.getPending().forEach {
            val timeOut = Config().getChainConfig(it.assetId.chain.string).transactionTimeout * 1000
            if (it.createdAt < System.currentTimeMillis() - timeOut) {
                localSource.updateTransaction(listOf(it.copy(state = TransactionState.Failed)))
            }
        }
    }

    private suspend fun checkTx(tx: Transaction): Transaction? {
        val stateClient = stateClients.firstOrNull { it.isMaintain(tx.assetId.chain) } ?: return null
        val stateResult = stateClient.getStatus(tx.from, tx.hash)
        val state = stateResult.getOrElse { TransactionChages(tx.state) }
        return if (state.state != tx.state) {
            val newTx = tx.copy(
                id = if (state.hashChanges != null) "${tx.assetId.chain.string}_${state.hashChanges!!.new}" else tx.id,
                state = state.state,
                hash = if (state.hashChanges != null) state.hashChanges!!.new else tx.hash,
            )
            when {
                tx.assetId.chain.eip1559Support() && state.fee != null -> newTx.copy(fee = state.fee.toString())
                else -> newTx
            }
        } else {
            null
        }
    }
}