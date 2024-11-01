package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.PriceAlertDirection

@Entity(tableName = "price_alerts")
data class DbPriceAlert(
    @PrimaryKey @ColumnInfo("asset_id") val assetId: String,
    val price: Double? = null,
    @ColumnInfo("price_percent_change")
    val pricePercentChange: Double? = null,
    @ColumnInfo("price_direction")
    val priceDirection: PriceAlertDirection? = null,
    val enabled: Boolean,
)