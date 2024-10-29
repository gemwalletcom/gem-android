package com.gemwallet.android.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.database.entities.DbAccount

@Dao
interface AccountsDao {
    @Query("SELECT * FROM accounts WHERE wallet_id = :walletId")
    fun getByWalletId(walletId: String): List<DbAccount>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(account: DbAccount)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(account: List<DbAccount>)

    @Delete
    fun delete(account: DbAccount)

    @Query("DELETE FROM accounts WHERE wallet_id=:walletId")
    fun deleteByWalletId(walletId: String)
}