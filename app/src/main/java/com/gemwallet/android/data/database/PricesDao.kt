package com.gemwallet.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.database.entities.DbPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface PricesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(priceRoom: DbPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(priceRoom: List<DbPrice>)

    @Query("SELECT * FROM prices")
    fun getAll(): Flow<List<DbPrice>>

    @Query("SELECT * FROM prices WHERE assetId IN (:assetsId)")
    suspend fun getByAssets(assetsId: List<String>): List<DbPrice>

    @Query("DELETE FROM prices")
    suspend fun deleteAll()
}