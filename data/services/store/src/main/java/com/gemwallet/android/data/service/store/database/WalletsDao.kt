package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbWallet

@Dao
interface WalletsDao {
    @Query("SELECT * FROM wallets")
    suspend fun getAll(): List<DbWallet>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: String): DbWallet?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(wallet: DbWallet)

    @Delete
    suspend fun delete(account: DbWallet)
}