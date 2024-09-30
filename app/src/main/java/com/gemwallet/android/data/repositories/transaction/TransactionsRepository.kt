package com.gemwallet.android.data.repositories.transaction

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.cases.transactions.CreateTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.cases.transactions.PutTransactionsCase
import com.gemwallet.android.data.asset.AssetsLocalSource
import com.gemwallet.android.data.database.TransactionsDao
import com.gemwallet.android.data.database.entities.DbTransactionExtended
import com.gemwallet.android.data.database.entities.DbTxSwapMetadata
import com.gemwallet.android.data.database.mappers.ExtendedTransactionMapper
import com.gemwallet.android.data.database.mappers.TransactionMapper
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.same
import com.gemwallet.android.ext.toAssetId
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
) : GetTransactionsCase, GetTransactionCase, CreateTransactionCase, PutTransactionsCase {
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

    override fun getChangedTransactions(): Flow<List<TransactionExtended>> {
        return changedTransactions
    }

    override fun getTransactions(assetId: AssetId?, state: TransactionState?): Flow<List<TransactionExtended>> {
        return transactionsDao.getExtendedTransactions()
            .map { txs -> txs.filter { state == null || it.state == state } }
            .mapNotNull { it.mapNotNull { tx -> mapper.asEntity(tx) } }
            .map { list ->
                list.filter {
                    (assetId == null
                        || it.asset.id.same(assetId)
                        || it.transaction.getSwapMetadata()?.toAsset?.same(assetId) == true
                        || it.transaction.getSwapMetadata()?.fromAsset?.same(assetId) == true
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

    override fun getTransaction(txId: String): Flow<TransactionExtended?> {
        return transactionsDao.getExtendedTransaction(txId)
            .mapNotNull { mapper.asEntity(it ?: return@mapNotNull null) }
    }

    override suspend fun putTransactions(walletId: String, transactions: List<Transaction>) = withContext(Dispatchers.IO) {
        val mapper = TransactionMapper(walletId)
        transactionsDao.insert(transactions.map { mapper.asDomain(it) })
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
        transactionsDao.insert(listOf(TransactionMapper(walletId).asDomain(transaction)))
        addSwapMetadata(listOf(transaction))
        transaction
    }

    private suspend fun observePending() = scope.launch {
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
            changedTransactions.tryEmit(updatedTxs.mapNotNull(mapper::asEntity))

            updateTransaction(updatedTxs)
        }
        val failedByTimeOut = (transactionsDao.getExtendedTransactions().firstOrNull() ?: emptyList())
            .filter {
                it.state == TransactionState.Pending
            }
            .mapNotNull {
                val assetId = it.assetId.toAssetId() ?: return@mapNotNull null
                val timeOut =
                    Config().getChainConfig(assetId.chain.string).transactionTimeout * 1000
                if (it.createdAt < System.currentTimeMillis() - timeOut) {
                    it.copy(state = TransactionState.Failed)
                } else {
                    null
                }
            }
        updateTransaction(failedByTimeOut)
        changedTransactions.tryEmit(failedByTimeOut.mapNotNull(mapper::asEntity))
    }

    private suspend fun updateTransaction(txs: List<DbTransactionExtended>) = withContext(Dispatchers.IO) {
        val data = txs.mapNotNull { tx ->
            mapper.asEntity(tx)?.transaction?.let {
                TransactionMapper(tx.walletId).asDomain(it)
            }
        }
        transactionsDao.insert(data)
    }

    private suspend fun checkTx(tx: DbTransactionExtended): DbTransactionExtended? {
        val assetId = tx.assetId.toAssetId() ?: return null
        val stateClient = stateClients.firstOrNull { it.isMaintain(assetId.chain) } ?: return null
        val stateResult = stateClient.getStatus(tx.owner, tx.hash)
        val state = stateResult.getOrElse { TransactionChages(tx.state) }
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
                assetId.chain.eip1559Support() && state.fee != null -> newTx.copy(fee = state.fee.toString())
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