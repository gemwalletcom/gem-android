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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TransactionsRepository(
    private val localSource: TransactionsLocalSource,
    private val stateClients: List<TransactionStatusClient>,
    private val assetsLocalSource: AssetsLocalSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val onRefreshTxs = mutableListOf<() -> Unit>()
    private val onRefreshAssets = mutableListOf<(List<TransactionExtended>) -> Unit>()

    init {
        scope.launch {
            while (true) {
                observePending()
                delay(10 * DateUtils.SECOND_IN_MILLIS)
            }
        }
    }

    fun subscribe(listener: () -> Unit) {
        onRefreshTxs.add(listener)
    }

    fun subscribe(listener: (List<TransactionExtended>) -> Unit) {
        onRefreshAssets.add(listener)
    }

    suspend fun getPending(owners: List<Account>): Flow<List<TransactionExtended>> = withContext(Dispatchers.IO) {
        localSource.getExtendedTransactions(emptyList(), *owners.toTypedArray())
            .map { txs -> txs.filter { it.transaction.state == TransactionState.Pending } }
    }

    suspend fun getTransactions(assetId: AssetId? = null, vararg accounts: Account): Flow<List<TransactionExtended>> = withContext(Dispatchers.IO) {
        val chains = accounts.map { it.chain }
        localSource.getExtendedTransactions(emptyList(),  *accounts)
            .map { list ->
                list.filter {
                    chains.contains(it.asset.id.chain) &&
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

    suspend fun getTransaction(txId: String): Flow<TransactionExtended?> {
        return localSource.getExtendedTransactions(listOf(txId)).map { it.firstOrNull() }
            .flowOn(Dispatchers.IO)
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
        val extTxs = localSource.getExtendedTransactions(listOf(transaction.id)).firstOrNull() ?: emptyList()
        onRefreshAssets.forEach { it(extTxs) }
        onRefreshTxs.forEach { it() }
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
            val extTxs = localSource.getExtendedTransactions(updatedTxs.map { it.id }).firstOrNull() ?: emptyList()
            onRefreshAssets.forEach { it(extTxs) }
            onRefreshTxs.forEach { it() }
        }
        localSource.getPending().forEach {
            if (it.createdAt < System.currentTimeMillis() - uniffi.Gemstone.Config().getChainConfig(it.assetId.chain.string).transactionTimeout * 1000) {
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