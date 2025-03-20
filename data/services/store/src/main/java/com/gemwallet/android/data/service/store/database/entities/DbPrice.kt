package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

@Entity(tableName = "prices")
data class DbPrice(
    @PrimaryKey @ColumnInfo("asset_id") val assetId: String,
    val value: Double? = 0.0,
    @ColumnInfo("day_changed") val dayChanged: Double? = 0.0,
    val currency: String,
)

fun AssetPrice.toRecord(currency: Currency): DbPrice {
    return DbPrice(
        assetId = assetId,
        value = price,
        dayChanged = priceChangePercentage24h,
        currency = currency.string
    )
}

fun List<AssetPrice>.toRecord(currency: Currency) = map { it.toRecord(currency) }