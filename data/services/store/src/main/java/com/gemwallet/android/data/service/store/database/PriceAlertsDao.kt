package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbPriceAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(alerts: List<DbPriceAlert>)

    @Query(
        """
        SELECT * FROM price_alerts WHERE enabled = 1
        """
    )
    fun getAlerts(): Flow<List<DbPriceAlert>>

    @Query(
        """
        SELECT * FROM price_alerts WHERE asset_id = :assetId
        """
    )
    fun getAlert(assetId: String): Flow<DbPriceAlert?>

    @Query(
        """
        UPDATE price_alerts SET enabled = :enabled WHERE asset_id = :assetId
        """
    )
    suspend fun enabled(assetId: String, enabled: Boolean)
}