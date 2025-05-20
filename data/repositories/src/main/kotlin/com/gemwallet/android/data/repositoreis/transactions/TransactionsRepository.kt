package com.gemwallet.android.data.repositoreis.transactions

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
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
import com.gemwallet.android.model.TransactionChages
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
import uniffi.gemstone.Config
import java.math.BigInteger

class TransactionsRepository(
    private val transactionsDao: TransactionsDao,
    assetsDao: AssetsDao,
    private val stateClients: List<TransactionStatusClient>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : GetTransactions, GetTransaction, CreateTransaction, PutTransactions, GetTransactionUpdateTime {

    private val assetsRoomSource = GetAssetByIdCase(assetsDao)
    private val changedTransactions = MutableStateFlow<List<TransactionExtended>>(emptyList()) // TODO: Update balances.

    init {
        scope.launch {
            while (true) {
                observePending()
                delay(10 * DateUtils.SECOND_IN_MILLIS)
            }
        }
    }

    override fun getChangedTransactions(): Flow<List<TransactionExtended>> {
        return changedTransactions
    }

    override fun getTransactionUpdateTime(walletId: String): Long {
        return transactionsDao.getUpdateTime(walletId)
    }

    override fun getPendingTransactions(): Flow<Int?> {
        return transactionsDao.getPendingCount()
    }

    override fun getTransactions(assetId: AssetId?, state: TransactionState?): Flow<List<TransactionExtended>> {
        return transactionsDao.getExtendedTransactions()
            .map { txs -> txs.filter { state == null || it.state == state } }
            .mapNotNull { it.toModel() }
            .map { items ->
                items.filter {
                    val swapMetadata = it.transaction.getSwapMetadata()
                    (assetId == null
                        || it.asset.id == assetId
                        || swapMetadata?.toAsset == assetId
                        || swapMetadata?.fromAsset == assetId
                    )
                }.map {
                    val metadata = it.transaction.getSwapMetadata()
                    if (metadata != null) {
                        it.copy(
                            assets = listOf(
                                assetsRoomSource.getById(metadata.fromAsset),
                                assetsRoomSource.getById(metadata.toAsset),
                            ).mapNotNull { asset -> asset }
                        )
                    } else {
                        it
                    }
                }
            }
    }

    override fun getTransaction(txId: String): Flow<TransactionExtended?> {
        return transactionsDao.getExtendedTransaction(txId)
            .mapNotNull { it?.toModel() }
    }

    override suspend fun putTransactions(walletId: String, transactions: List<Transaction>) = withContext(Dispatchers.IO) {
        transactionsDao.insert(transactions.toRecord(walletId))
        addSwapMetadata(transactions.filter { it.type == TransactionType.Swap })
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

    private fun observePending() = scope.launch {
        // TODO: Update stake state
        val pendingTxs = transactionsDao.getExtendedTransactions().firstOrNull()?.filter {
            it.state == TransactionState.Pending
        } ?: emptyList()
        val updatedTxs = pendingTxs.map { tx ->
            async {
                val newTx = checkTx(tx)
                if (newTx != null && newTx.id != tx.id) {
                    transactionsDao.delete(tx.id)
                }
                newTx
            }
        }
        .awaitAll()
        .filterNotNull()
        if (updatedTxs.isNotEmpty()) {
            changedTransactions.tryEmit(updatedTxs.toModel())

            updateTransaction(updatedTxs)
        }
        val failedByTimeout = (transactionsDao.getExtendedTransactions().firstOrNull() ?: emptyList())
            .filter { it.state == TransactionState.Pending }
            .mapNotNull {
                val assetId = it.assetId.toAssetId() ?: return@mapNotNull null
                val timeout = Config().getChainConfig(assetId.chain.string).transactionTimeout.toLong() * 1000L

                if (it.createdAt < System.currentTimeMillis() - timeout) { //TODO: Change to update time
                    it.copy(state = TransactionState.Failed)
                } else {
                    null
                }
            }
        updateTransaction(failedByTimeout)
        changedTransactions.tryEmit(failedByTimeout.toModel())
    }

    private suspend fun updateTransaction(txs: List<DbTransactionExtended>) = withContext(Dispatchers.IO) {
        val data = txs.mapNotNull { it.toModel()?.transaction?.toRecord(it.walletId) }
        transactionsDao.insert(data)
    }

    private suspend fun checkTx(tx: DbTransactionExtended): DbTransactionExtended? {
        val assetId = tx.assetId.toAssetId() ?: return null
        val stateClient = stateClients.firstOrNull { it.supported(assetId.chain) } ?: return null
        val state = try {
            stateClient.getStatus(
                TransactionStateRequest(
                    chain = assetId.chain,
                    sender = tx.owner,
                    hash = tx.hash,
                    block = tx.blockNumber,
                )
            )
        } catch (_: ServiceUnavailable) {
            return tx.copy(updatedAt = System.currentTimeMillis())
        } catch (_: Throwable) {
            TransactionChages(tx.state)
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

    private fun addSwapMetadata(txs: List<Transaction>) {
        val room = txs.filter { it.type == TransactionType.Swap && it.metadata != null }.mapNotNull {
            val txMetadata = it.metadata?.let { jsonEncoder.decodeFromString<TransactionSwapMetadata>(it) } ?: return@mapNotNull null
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