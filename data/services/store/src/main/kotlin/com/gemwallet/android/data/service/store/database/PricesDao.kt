package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbFiatRate
import com.gemwallet.android.data.service.store.database.entities.DbPrice
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface PricesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(priceRoom: DbPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(priceRoom: List<DbPrice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setRates(rates: List<DbFiatRate>)

    @Query("SELECT * FROM prices")
    fun getAll(): Flow<List<DbPrice>>

    @Query("SELECT * FROM prices WHERE asset_id IN (:assetsId)")
    suspend fun getByAssets(assetsId: List<String>): List<DbPrice>

    @Query("DELETE FROM prices")
    suspend fun deleteAll()

    @Query("SELECT * FROM currency_rates WHERE currency=:currency")
    suspend fun getRates(currency: Currency): DbFiatRate?
}