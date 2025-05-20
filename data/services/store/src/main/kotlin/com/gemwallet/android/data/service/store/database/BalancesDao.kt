package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.service.store.database.entities.DbBalance

@Dao
interface BalancesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: DbBalance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: List<DbBalance>)

    @Update
    fun update(balance: DbBalance)

    @Query("SELECT * FROM balances WHERE wallet_id = :walletId AND account_address = :accountAddresses AND asset_id = :assetId")
    fun getByAccount(walletId: String, accountAddresses: String, assetId: String): DbBalance?
}