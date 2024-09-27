package com.gemwallet.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.database.entities.DbTransaction
import com.gemwallet.android.data.database.entities.DbTransactionExtended
import com.gemwallet.android.data.database.entities.DbTxSwapMetadata
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {

    @Query("SELECT * FROM transactions WHERE state = :state")
    fun getByState(state: TransactionState): List<DbTransaction>

    @Insert(entity = DbTransaction::class, onConflict = OnConflictStrategy.REPLACE)
    fun insert(transactions: List<DbTransaction>)

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

    @Insert(entity = DbTxSwapMetadata::class, onConflict = OnConflictStrategy.REPLACE)
    fun addSwapMetadata(metadata: List<DbTxSwapMetadata>)

    @Query("SELECT * FROM tx_swap_metadata WHERE tx_id=:txId")
    fun getMetadata(txId: String): DbTxSwapMetadata?
}