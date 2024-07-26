package com.gemwallet.android.data.transaction

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.gemwallet.android.data.database.entities.DbTransactionExtended
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.google.gson.Gson
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Price
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

@Entity(tableName = "transactions")
data class TransactionRoom(
    @PrimaryKey val id: String,
    val hash: String,
    val assetId: String,
    val feeAssetId: String,
    val owner: String,
    val recipient: String,
    val contract: String? = null,
    val metadata: String? = null,
    val state: TransactionState,
    val type: TransactionType,
    val blockNumber: String,
    val sequence: String,
    val fee: String, // Atomic value - BigInteger
    val value: String, // Atomic value - BigInteger
    val payload: String? = null,
    val direction: TransactionDirection,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "tx_swap_metadata")
data class TxSwapMetadataRoom(
    @PrimaryKey @ColumnInfo(name = "tx_id") val txId: String,
    @ColumnInfo(name = "from_asset_id") val fromAssetId: String,
    @ColumnInfo(name = "to_asset_id") val toAssetId: String,
    @ColumnInfo(name = "from_amount") val fromAmount: String,
    @ColumnInfo(name = "to_amount") val toAmount: String,
)

@Dao
interface TransactionsDao {

    @Query("SELECT * FROM transactions WHERE state = :state")
    fun getByState(state: TransactionState): List<TransactionRoom>

    @Insert(entity = TransactionRoom::class, onConflict = OnConflictStrategy.REPLACE)
    fun insert(transactions: List<TransactionRoom>)

    @Query("DELETE FROM transactions WHERE id=:id")
    fun delete(id: String)

    @Query("SELECT * FROM extended_txs ORDER BY createdAt DESC")
    fun getExtendedTransactions(): Flow<List<DbTransactionExtended>>

    @Query("SELECT * FROM extended_txs WHERE id IN (:ids) ORDER BY createdAt DESC")
    fun getExtendedTransactions(ids: List<String>): Flow<List<DbTransactionExtended>>

    @Query("SELECT * FROM extended_txs WHERE id = :id")
    fun getExtendedTransaction(id: String): Flow<DbTransactionExtended?>

    @Query("SELECT * FROM extended_txs WHERE state = :state ORDER BY createdAt DESC")
    fun getTransactionsByState(state: TransactionState): Flow<List<DbTransactionExtended>>

    @Insert(entity = TxSwapMetadataRoom::class, onConflict = OnConflictStrategy.REPLACE)
    fun addSwapMetadata(metadata: List<TxSwapMetadataRoom>)

    @Query("SELECT * FROM tx_swap_metadata WHERE tx_id=:txId")
    fun getMetadata(txId: String): TxSwapMetadataRoom?
}

class TransactionsRoomSource(
    private val transactionsDao: TransactionsDao,
    private val gson: Gson,
) : TransactionsLocalSource {

    override suspend fun putTransactions(transactions: List<Transaction>) {
        transactionsDao.insert(
            transactions.map { toRoom(it) }
        )
        addSwapMetadata(transactions.filter { it.type == TransactionType.Swap })
    }

    override suspend fun addTransaction(transaction: Transaction): Boolean = withContext(Dispatchers.IO) {
        transactionsDao.insert(listOf(toRoom(transaction)))
        addSwapMetadata(listOf(transaction))
        true
    }

    private fun addSwapMetadata(txs: List<Transaction>) {
        val room = txs.filter { it.type == TransactionType.Swap && it.metadata != null }.map  {
            val txMetadata = gson.fromJson(it.metadata, TransactionSwapMetadata::class.java)
            txMetadata.toRoom(it.id)
        }
        transactionsDao.addSwapMetadata(room)
    }

    override suspend fun updateTransaction(txs: List<Transaction>): Boolean {
        transactionsDao.insert(txs.map(this::toRoom))
        return true
    }

    override fun getExtendedTransactions(): Flow<List<TransactionExtended>> {
        return transactionsDao.getExtendedTransactions()
            .mapNotNull { it.mapNotNull { tx -> toExtendedTransaction(tx) } }
    }

    override fun getExtendedTransaction(txId: String): Flow<TransactionExtended?> {
        return transactionsDao.getExtendedTransaction(txId)
            .mapNotNull { toExtendedTransaction(it ?: return@mapNotNull null) }
    }

    override fun getTransactionsByState(state: TransactionState): Flow<List<TransactionExtended>> {
        return transactionsDao.getTransactionsByState(state)
            .mapNotNull { it.mapNotNull { tx -> toExtendedTransaction(tx) } }
    }

    override suspend fun remove(tx: Transaction) {
        transactionsDao.delete(tx.id)
    }

    override suspend fun getMetadata(txId: String): Any? {
        return transactionsDao.getMetadata(txId)
    }

    private fun toRoom(transaction: Transaction) = TransactionRoom(
        id = transaction.id,
        hash = transaction.hash,
        assetId = transaction.assetId.toIdentifier(),
        feeAssetId = transaction.feeAssetId.toIdentifier(),
        owner = transaction.from,
        recipient = transaction.to,
        contract = transaction.contract,
        type = transaction.type,
        state = transaction.state,
        blockNumber = transaction.blockNumber,
        sequence = transaction.sequence,
        fee = transaction.fee,
        value = transaction.value,
        payload = transaction.memo,
        metadata = transaction.metadata,
        direction = transaction.direction,
        updatedAt = System.currentTimeMillis(),
        createdAt = transaction.createdAt,
    )

    private fun toTransaction(room: TransactionRoom): Transaction? {
        return Transaction(
            id = room.id,
            hash = room.hash,
            assetId = room.assetId.toAssetId() ?: return null,
            from = room.owner,
            to = room.recipient,
            contract = room.contract,
            type = room.type,
            state = room.state,
            blockNumber = room.blockNumber,
            sequence = room.sequence,
            fee = room.fee,
            feeAssetId = room.feeAssetId.toAssetId() ?: return null,
            value = room.value,
            memo = room.payload,
            direction = room.direction,
            utxoInputs = emptyList(),
            utxoOutputs = emptyList(),
            createdAt = if (room.createdAt == 0L) System.currentTimeMillis() else room.createdAt,
            metadata = room.metadata,
        )
    }

    private fun toExtendedTransaction(room: DbTransactionExtended): TransactionExtended? {
        return TransactionExtended(
            transaction = Transaction(
                id = room.id,
                hash = room.hash,
                assetId = room.assetId.toAssetId() ?: return null,
                from = room.owner,
                to = room.recipient,
                contract = room.contract,
                type = room.type,
                state = room.state,
                blockNumber = room.blockNumber,
                sequence = room.sequence,
                fee = room.fee,
                feeAssetId = room.feeAssetId.toAssetId() ?: return null,
                value = room.value,
                memo = room.payload,
                direction = room.direction,
                utxoInputs = emptyList(),
                utxoOutputs = emptyList(),
                createdAt = room.createdAt,
                metadata = room.metadata,
            ),
            asset = Asset(
                id = room.assetId.toAssetId() ?: return null,
                name = room.assetName,
                symbol = room.assetSymbol,
                decimals = room.assetDecimals,
                type = room.assetType,
            ),
            feeAsset = Asset(
                id = room.feeAssetId.toAssetId() ?: return null,
                name = room.feeName,
                symbol = room.feeSymbol,
                decimals = room.feeDecimals,
                type = room.feeType,
            ),
            price = if (room.assetPrice == null)
                null
            else
                Price(room.assetPrice, room.assetPriceChanged ?: 0.0),
            feePrice = if (room.feePrice == null)
                null
            else
                Price(room.feePrice, room.feePriceChanged ?: 0.0),
            assets = emptyList(),
        )
    }

    private fun TransactionSwapMetadata.toRoom(txId: String): TxSwapMetadataRoom {
        return TxSwapMetadataRoom(
            txId = txId,
            fromAssetId = fromAsset.toIdentifier(),
            toAssetId = toAsset.toIdentifier(),
            fromAmount = fromValue,
            toAmount = toValue,
        )
    }
}