package com.gemwallet.android.data.repositories.transaction

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.data.asset.AssetsLocalSource
import com.gemwallet.android.data.database.TransactionsDao
import com.gemwallet.android.data.database.entities.DbTxSwapMetadata
import com.gemwallet.android.data.database.mappers.ExtendedTransactionMapper
import com.gemwallet.android.data.database.mappers.TransactionMapper
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TransactionChages
import com.google.gson.Gson
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.Gemstone.Config
import java.math.BigInteger

class TransactionsRepository(
    private val transactionsDao: TransactionsDao,
    private val stateClients: List<TransactionStatusClient>,
    private val assetsLocalSource: AssetsLocalSource,
    private val gson: Gson,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val changedTransactions = MutableStateFlow<List<TransactionExtended>>(emptyList())
    private val mapper = ExtendedTransactionMapper()

    init {
        scope.launch {
            while (true) {
                observePending()
                delay(10 * DateUtils.SECOND_IN_MILLIS)
            }
        }
    }

    fun getChangedTransactions(): Flow<List<TransactionExtended>> {
        return changedTransactions
    }

    fun getPendingTransactions(): Flow<List<TransactionExtended>> {
        return transactionsDao.getTransactionsByState(TransactionState.Pending)
            .mapNotNull { it.mapNotNull { tx -> mapper.asEntity(tx) } }
    }

    fun getTransactions(assetId: AssetId? = null): Flow<List<TransactionExtended>> {
        return transactionsDao.getExtendedTransactions()
            .mapNotNull { it.mapNotNull { tx -> mapper.asEntity(tx) } }
            .map { list ->
                list.filter {
                    (assetId == null
                            || it.asset.id.toIdentifier() == assetId.toIdentifier()
                            || it.transaction.getSwapMetadata()?.toAsset?.toIdentifier() == assetId.toIdentifier()
                            || it.transaction.getSwapMetadata()?.fromAsset?.toIdentifier() == assetId.toIdentifier()
                            )
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
        return transactionsDao.getExtendedTransaction(txId)
            .mapNotNull { mapper.asEntity(it ?: return@mapNotNull null) }
    }

    suspend fun putTransactions(transactions: List<Transaction>) = withContext(Dispatchers.IO) {
        transactionsDao.insert(
            transactions.map { TransactionMapper().asDomain(it) }
        )
        addSwapMetadata(transactions.filter { it.type == TransactionType.Swap })
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
        addTransaction(transaction)
        Result.success(transaction)
    }

    private suspend fun observePending() = scope.launch {
        val pendings = getPendingTransactions().firstOrNull() ?: emptyList()
        val updatedTxs = pendings.map { tx ->
            async {
                val newTx = checkTx(tx.transaction)
                if (newTx != null && newTx.id != tx.transaction.id) {
                    transactionsDao.delete(tx.transaction.id)
                }
                tx.copy(transaction = newTx ?: return@async null)
            }
        }
            .awaitAll()
            .filterNotNull()
        if (updatedTxs.isNotEmpty()) {
            changedTransactions.tryEmit(updatedTxs)
            updateTransaction(updatedTxs.map { it.transaction })
        }
        val failedByTimeOut = (getPendingTransactions().firstOrNull() ?: emptyList()).mapNotNull {
            val timeOut =
                Config().getChainConfig(it.transaction.assetId.chain.string).transactionTimeout * 1000
            if (it.transaction.createdAt < System.currentTimeMillis() - timeOut) {
                it.copy(transaction = it.transaction.copy(state = TransactionState.Failed))
            } else {
                null
            }
        }
        updateTransaction(failedByTimeOut.map { it.transaction })
        changedTransactions.tryEmit(failedByTimeOut)
    }

    private suspend fun addTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionsDao.insert(listOf(TransactionMapper().asDomain(transaction)))
        addSwapMetadata(listOf(transaction))
    }

    private suspend fun updateTransaction(txs: List<Transaction>) = withContext(Dispatchers.IO) {
        val mapper = TransactionMapper()
        transactionsDao.insert(txs.map(mapper::asDomain))
    }

    private suspend fun checkTx(tx: Transaction): Transaction? {
        val stateClient =
            stateClients.firstOrNull { it.isMaintain(tx.assetId.chain) } ?: return null
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

    private fun addSwapMetadata(txs: List<Transaction>) {
        val room = txs.filter { it.type == TransactionType.Swap && it.metadata != null }.map {
            val txMetadata = gson.fromJson(it.metadata, TransactionSwapMetadata::class.java)
            DbTxSwapMetadata(
                txId = it.id,
                fromAssetId = txMetadata.fromAsset.toIdentifier(),
                toAssetId = txMetadata.toAsset.toIdentifier(),
                fromAmount = txMetadata.fromValue,
                toAmount = txMetadata.toValue,
            )
        }
        transactionsDao.addSwapMetadata(room)
    }
}