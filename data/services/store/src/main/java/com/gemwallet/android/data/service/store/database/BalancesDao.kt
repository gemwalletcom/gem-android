package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.service.store.database.entities.DbBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface BalancesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: DbBalance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: List<DbBalance>)

    @Update
    fun update(balance: DbBalance)

    @Query("SELECT * FROM balances WHERE account_address = :addresses AND asset_id = :assetId")
    fun getByAccount(addresses: String, assetId: String): DbBalance?
}