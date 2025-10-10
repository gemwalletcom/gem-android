package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbAccount

@Dao
interface AccountsDao {
    @Query("SELECT * FROM accounts WHERE wallet_id = :walletId")
    suspend fun getByWalletId(walletId: String): List<DbAccount>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(account: DbAccount)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(account: List<DbAccount>)

    @Delete
    suspend fun delete(account: DbAccount)

    @Query("DELETE FROM accounts WHERE wallet_id=:walletId")
    suspend fun deleteByWalletId(walletId: String)
}