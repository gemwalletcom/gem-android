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

    @Query("SELECT * FROM balances WHERE owner IN (:addresses)")
    fun getAllByOwner(addresses: List<String>): Flow<List<DbBalance>>

    @Query("SELECT * FROM balances WHERE owner IN (:addresses) AND asset_id IN (:assetId)")
    fun getByAssetId(addresses: List<String>, assetId: List<String>): List<DbBalance>
}