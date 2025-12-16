package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface PerpetualBalanceDao {
    @Insert(onConflict = REPLACE)
    suspend fun put(items: DbPerpetualBalance)

    @Delete
    suspend fun remove(items: List<DbPerpetualBalance>)

    @Query("""
        SELECT * FROM perpetual_balance WHERE accountAddress IN (:accountAddresses)
    """)
    fun getBalances(accountAddresses: List<String>): Flow<List<DbPerpetualBalance>>

    @Query("""
        SELECT * FROM perpetual_balance WHERE accountAddress = :accountAddresses
    """)
    fun getBalance(accountAddresses: String): Flow<DbPerpetualBalance>

}