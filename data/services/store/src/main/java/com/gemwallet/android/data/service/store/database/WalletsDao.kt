package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbAccount
import com.gemwallet.android.data.service.store.database.entities.DbWallet
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletsDao {
    @Query("""
        SELECT * FROM wallets
        JOIN accounts ON wallets.id = accounts.wallet_id
    """)
    fun getAll(): Flow<Map<DbWallet, List<DbAccount>>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    fun getById(id: String): Flow<DbWallet?>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(wallet: DbWallet)

    @Delete
    suspend fun delete(account: DbWallet)
}