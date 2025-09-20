package com.gemwallet.android.data.repositoreis.transactions

import android.text.format.DateUtils
import android.util.Log
import com.gemwallet.android.blockchain.model.ServiceUnavailable
import com.gemwallet.android.blockchain.model.TransactionStateRequest
import com.gemwallet.android.blockchain.services.TransactionStatusService
import com.gemwallet.android.cases.transactions.ClearPendingTransactions
import com.gemwallet.android.cases.transactions.CreateTransaction
import com.gemwallet.android.cases.transactions.GetTransaction
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.cases.transactions.PutTransactions
import com.gemwallet.android.data.repositoreis.assets.GetAssetByIdCase
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.TransactionsDao
import com.gemwallet.android.data.service.store.database.entities.DbTransactionExtended
import com.gemwallet.android.data.service.store.database.entities.DbTxSwapMetadata
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.Transaction
import com.gemwallet.android.model.TransactionChanges
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsRepository(
    private val transactionsDao: TransactionsDao,
    private val transactionStatusService: TransactionStatusService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    assetsDao: AssetsDao,
)
: GetTransactions,
    GetTransaction,
    CreateTransaction,
    PutTransactions,
    GetTransactionUpdateTime,
    ClearPendingTransactions
{

    private val tenSeconds = 10 * DateUtils.SECOND_IN_MILLIS

    private val assetsRoomSource = GetAssetByIdCase(assetsDao)
    val changedTransactions = MutableStateFlow<List<TransactionExtended>>(emptyList())
    private val pendingTransactionJobs = ConcurrentHashMap<String, Job>()

    init {
        handlePendingTransactions()
    }

    override fun getTransactionUpdateTime(walletId: String): Long {
        return transactionsDao.getUpdateTime(walletId)
    }

    override fun getPendingTransactionsCount(): Flow<Int?> {
        return transactionsDao.getPendingCount()
    }

    override fun getTransactions(assetId: AssetId?, state: TransactionState?): Flow<List<TransactionExtended>> {
        return transactionsDao.getExtendedTransactions()
            .flowOn(Dispatchers.IO)
            .map { txs -> txs.filter { state == null || it.state == state } }
            .mapNotNull { it.toModel() }
            .map { items ->
                items.filter {
                    val swapMetadata = it.transaction.getSwapMetadata()
                    assetId == null || it.asset.id == assetId
                        || swapMetadata?.toAsset == assetId
                        || swapMetadata?.fromAsset == assetId
                }.map {
                    val metadata = it.transaction.getSwapMetadata()
                    if (metadata != null) {
                        it.copy(
                            assets = listOfNotNull(
                                assetsRoomSource.getById(metadata.fromAsset),
                                assetsRoomSource.getById(metadata.toAsset),
                            )
                        )
                    } else {
                        it
                    }
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getTransaction(txId: String): Flow<TransactionExtended?> {
        return transactionsDao.getExtendedTransaction(txId)
            .mapNotNull { it?.toModel() }
            .flowOn(Dispatchers.IO)
    }

    override fun getChangedTransactions(): Flow<List<TransactionExtended>> = changedTransactions

    override suspend fun putTransactions(walletId: String, transactions: List<Transaction>) = withContext(Dispatchers.IO) {
        transactionsDao.insert(transactions.toRecord(walletId))
        addSwapMetadata(transactions.filter { it.type == TransactionType.Swap })
    }

    private suspend fun updateTransaction(txs: List<DbTransactionExtended>) = withContext(Dispatchers.IO) {
        val data = txs.mapNotNull { it.toModel()?.transaction?.toRecord(it.walletId) }
        transactionsDao.insert(data)
    }

    override suspend fun clearPending() {
        transactionsDao.removePendingTransactions()
    }

    override suspend fun createTransaction(
        hash: String,
        walletId: String,
        assetId: AssetId,
        owner: Account,
        to: String,
        state: TransactionState,
        fee: Fee,
        amount: BigInteger,
        memo: String?,
        type: TransactionType,
        metadata: String?,
        direction: TransactionDirection,
        blockNumber: String,
    ): Transaction = withContext(Dispatchers.IO) {
        val transaction = Transaction(
            id = "${assetId.chain.string}_$hash",
            hash = hash,
            assetId = assetId,
            feeAssetId = fee.feeAssetId,
            from = owner.address,
            to = to,
            type = type,
            state = state,
            blockNumber = blockNumber,
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
        transactionsDao.insert(listOf(transaction.toRecord(walletId)))
        addSwapMetadata(listOf(transaction))
        transaction
    }

    private fun addSwapMetadata(txs: List<Transaction>) {
        val room = txs.filter { it.type == TransactionType.Swap && it.metadata != null }.mapNotNull {
            val txMetadata = it.metadata?.let { metadata ->
                jsonEncoder.decodeFromString<TransactionSwapMetadata>(metadata)
            } ?: return@mapNotNull null
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

    private fun handlePendingTransactions() {
        scope.launch {
            transactionsDao.getExtendedTransactions(TransactionState.Pending).collect { items ->
                items.forEach { item ->
                    if (!pendingTransactionJobs.containsKey(item.id)) {
                        val job = handlePendingTransaction(item)
                        pendingTransactionJobs.put(item.id, job)
                    }
                }
            }
        }
    }

    private fun handlePendingTransaction(tx: DbTransactionExtended) = scope.launch {
        var iteration = 0L
        val assetId = tx.assetId.toAssetId() ?: return@launch
        val chainConfig = Config().getChainConfig(assetId.chain.string)
        val delay = chainConfig.blockTime.toLong()
        val timeout = chainConfig.transactionTimeout.toLong() * 1000L

        while (true) {
            Log.d("TRANSACTIONS", "Iteration: $iteration; Hash: ${tx.hash}")
            transactionCheckDelay(delay, iteration)
            iteration++

            val tx = checkTx(tx)?.let { newTx ->
                if (newTx.id != tx.id) {
                    transactionsDao.delete(tx.id)
                }
                updateTransaction(listOf(newTx))
                newTx
            } ?: tx

            if (tx.createdAt < System.currentTimeMillis() - timeout) {
                updateTransaction(listOf(tx.copy(state = TransactionState.Failed)))
                break
            }
            if (tx.state != TransactionState.Pending) {
                break
            }
        }
        tx.toModel()?.let { changedTransactions.tryEmit(listOf(it)) }
        pendingTransactionJobs.remove(tx.id)
    }

    private suspend fun transactionCheckDelay(delay: Long, iteration: Long) {
        val multiple = when (iteration) {
            0L -> 1.2
            1L -> 1.5
            2L -> 2.0
            3L -> 5.0
            else -> 10.0
        }
        val delay = (delay * multiple).toLong().takeIf { it < tenSeconds } ?: tenSeconds
        delay(delay)
    }

    private suspend fun checkTx(tx: DbTransactionExtended): DbTransactionExtended? {
        val assetId = tx.assetId.toAssetId() ?: return null
        val request = TransactionStateRequest(
            chain = assetId.chain,
            sender = tx.owner,
            hash = tx.hash,
            block = tx.blockNumber,
        )
        val state = try {
            transactionStatusService.getStatus(request) ?: TransactionChanges(tx.state)
        } catch (_: ServiceUnavailable) {
            return tx.copy(updatedAt = System.currentTimeMillis())
        } catch (_: Throwable) {
            TransactionChanges(tx.state)
        }
        return if (state.state != tx.state) {
            val newTx = tx.copy(
                id = if (state.hashChanges != null) {
                    "${assetId.chain.string}_${state.hashChanges!!.new}"
                } else {
                    tx.id
                },
                state = state.state,
                hash = if (state.hashChanges != null) state.hashChanges!!.new else tx.hash,
            )
            when {
                state.fee != null -> newTx.copy(fee = state.fee.toString())
                else -> newTx
            }
        } else {
            null
        }
    }
}