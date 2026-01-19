package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.service.store.database.entities.DbPriceAlert
import com.wallet.core.primitives.PriceAlertDirection
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertsDao {

    @Query("DELETE FROM price_alerts")
    suspend fun clear()

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
        SELECT * FROM price_alerts WHERE enabled = 1 AND assetId = :assetId
        """
    )
    fun getAlerts(assetId: String): Flow<List<DbPriceAlert>>

    @Query(
        """
        SELECT * FROM price_alerts WHERE assetId = :assetId AND price IS NULL AND pricePercentChange IS NULL AND priceDirection IS NULL AND enabled = 1
        """
    )
    fun getAssetPriceAlert(assetId: String): Flow<DbPriceAlert?>

    @Query(
        """SELECT * FROM price_alerts
            WHERE assetId = :assetId
                AND currency = :currency
                AND (price = :price OR (:price IS NULL AND price IS NULL))
                AND (priceDirection = :priceDirection OR (:priceDirection IS NULL AND priceDirection IS NULL))
                AND (pricePercentChange = :pricePercentChange OR (:pricePercentChange IS NULL AND pricePercentChange IS NULL))
        """
    )
    suspend fun findSamePriceAlert(assetId: String, currency: String, price: Double?, priceDirection: PriceAlertDirection?, pricePercentChange: Double?): List<DbPriceAlert>

    @Query(
        """
        UPDATE price_alerts SET enabled = :enabled WHERE assetId = :assetId
        """
    )
    suspend fun enabled(assetId: String, enabled: Boolean)

    @Query(
        """
        UPDATE price_alerts SET enabled = :enabled WHERE id = :priceAlertId
        """
    )
    suspend fun enabled(priceAlertId: Int, enabled: Boolean)

    @Update
    suspend fun update(items: List<DbPriceAlert>)

    @Query("SELECT * FROM price_alerts WHERE id = :priceAlertId")
    fun getPriceAlert(priceAlertId: Int): DbPriceAlert?
}