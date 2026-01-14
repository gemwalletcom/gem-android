package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbPriceAlert
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection
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
        SELECT * FROM price_alerts WHERE assetId = :assetId
        """
    )
    fun getAlert(assetId: String): Flow<DbPriceAlert?>

    @Query(
        """SELECT * FROM price_alerts
            WHERE assetId = :assetId
                AND currency = :currency
                AND price = :price
                AND priceDirection = :priceDirection
                AND pricePercentChange = :pricePercentChange
        """
    )
    suspend fun findSamePriceAlert(assetId: String, currency: String, price: Double?, priceDirection: PriceAlertDirection?, pricePercentChange: Double?): List<DbPriceAlert>

    @Query(
        """
        UPDATE price_alerts SET enabled = :enabled WHERE assetId = :assetId
        """
    )
    suspend fun enabled(assetId: String, enabled: Boolean)
}