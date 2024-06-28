package com.gemwallet.android.data.transaction

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.google.gson.Gson
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
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

data class TransactionExtendedRoom(
    val id: String,
    val hash: String,
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
)

const val SESSION_REQUEST = """SELECT accounts.address FROM accounts, session
    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1"""

@Dao
interface TransactionsDao {

    @Query("SELECT * FROM transactions WHERE state = :state")
    fun getByState(state: TransactionState): List<TransactionRoom>

    @Query("SELECT * FROM transactions WHERE (owner IN (:owners) OR recipient in (:owners)) AND state = :state ORDER BY createdAt DESC")
    fun getByState(owners: List<String>, state: TransactionState): Flow<List<TransactionRoom>>

    @Insert(entity = TransactionRoom::class, onConflict = OnConflictStrategy.REPLACE)
    fun insert(transactions: List<TransactionRoom>)

    @Update(entity = TransactionRoom::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(transaction: List<TransactionRoom>)

    @Query("DELETE FROM transactions WHERE id=:id")
    fun delete(id: String)

    @Query("""
        SELECT DISTINCT tx.id, tx.hash,
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
            assets.decimals as assetDecimals,
            assets.name as assetName,
            assets.type as assetType,
            assets.symbol as assetSymbol,
            feeAsset.decimals as feeDecimals,
            feeAsset.name as feeName,
            feeAsset.type as feeType,
            feeAsset.symbol as feeSymbol,
            prices.value as assetPrice,
            prices.dayChanged as assetPriceChanged,
            feePrices.value as feePrice,
            feePrices.dayChanged as feePriceChanged
        FROM transactions as tx 
            INNER JOIN assets ON tx.assetId = assets.id 
            INNER JOIN assets as feeAsset ON tx.feeAssetId = feeAsset.id 
            LEFT JOIN prices ON tx.assetId = prices.assetId 
            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.assetId 
            WHERE tx.id IN (:ids) OR (tx.owner IN ($SESSION_REQUEST) OR tx.recipient in ($SESSION_REQUEST))
            GROUP BY tx.id ORDER BY tx.createdAt DESC
    """)
    fun getExtendedTransactions(ids: List<String>): Flow<List<TransactionExtendedRoom>>

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

    @Deprecated("Use the getExtendedTransactions()")
    override suspend fun getExtendedTransactions(txIds: List<String>, vararg accounts: Account): Flow<List<TransactionExtended>> {
        val transactions = transactionsDao.getExtendedTransactions(txIds)
        return transactions.mapNotNull { it.mapNotNull { tx -> toExtendedTransaction(tx) } }
    }

    override suspend fun getExtendedTransactions(txIds: List<String>): Flow<List<TransactionExtended>> {
        val transactions = transactionsDao.getExtendedTransactions(txIds)
        return transactions.mapNotNull { it.mapNotNull { tx -> toExtendedTransaction(tx) } }
    }

    override suspend fun getPending(): List<Transaction> {
        return transactionsDao.getByState(TransactionState.Pending)
            .mapNotNull(this::toTransaction)
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

    private fun toExtendedTransaction(room: TransactionExtendedRoom): TransactionExtended? {
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
            price = if (room.assetPrice == null) null else Price(room.assetPrice, room.assetPriceChanged ?: 0.0),
            feePrice = if (room.feePrice == null) null else Price(room.feePrice, room.feePriceChanged ?: 0.0),
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