package com.gemwallet.android.data.repositoreis.transactions

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.cases.transactions.CreateTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.cases.transactions.PutTransactionsCase
import com.gemwallet.android.data.repositoreis.assets.GetAssetByIdCase
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.TransactionsDao
import com.gemwallet.android.data.service.store.database.entities.DbTransactionExtended
import com.gemwallet.android.data.service.store.database.entities.DbTxSwapMetadata
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.data.service.store.database.mappers.ExtendedTransactionMapper
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.same
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.Transaction
import com.gemwallet.android.model.TransactionChages
import com.gemwallet.android.model.TransactionExtended
import com.google.gson.Gson
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
    private val gson: Gson,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : GetTransactionsCase, GetTransactionCase, CreateTransactionCase, PutTransactionsCase {

    private val assetsRoomSource = GetAssetByIdCase(assetsDao)
    private val changedTransactions = MutableStateFlow<List<TransactionExtended>>(emptyList()) // TODO: Update balances.

//    private val txMapper = TransactionMapper()
    private val extTxMapper = ExtendedTransactionMapper()

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
            .mapNotNull { it.mapNotNull { tx -> extTxMapper.asEntity(tx) } }
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
            .mapNotNull { extTxMapper.asEntity(it ?: return@mapNotNull null) }
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
            changedTransactions.tryEmit(updatedTxs.mapNotNull(extTxMapper::asEntity))

            updateTransaction(updatedTxs)
        }
        val failedByTimeout = (transactionsDao.getExtendedTransactions().firstOrNull() ?: emptyList())
            .filter { it.state == TransactionState.Pending }
            .mapNotNull {
                val assetId = it.assetId.toAssetId() ?: return@mapNotNull null
                val timeout = Config().getChainConfig(assetId.chain.string).transactionTimeout * 1000

                if (it.createdAt < System.currentTimeMillis() - timeout) {
                    it.copy(state = TransactionState.Failed)
                } else {
                    null
                }
            }
        updateTransaction(failedByTimeout)
        changedTransactions.tryEmit(failedByTimeout.mapNotNull(extTxMapper::asEntity))
    }

    private suspend fun updateTransaction(txs: List<DbTransactionExtended>) = withContext(Dispatchers.IO) {
        val data = txs.mapNotNull { tx ->
            extTxMapper.asEntity(tx)?.transaction?.toRecord(tx.walletId)
        }
        transactionsDao.insert(data)
    }

    private suspend fun checkTx(tx: DbTransactionExtended): DbTransactionExtended? {
        val assetId = tx.assetId.toAssetId() ?: return null
        val stateClient = stateClients.firstOrNull { it.supported(assetId.chain) } ?: return null
        val stateResult = stateClient.getStatus(
            TransactionStateRequest(
                chain = assetId.chain,
                sender = tx.owner,
                hash = tx.hash,
                block = tx.blockNumber,
            )
        )
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