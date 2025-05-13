package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatRate

@Entity(tableName = "prices")
data class DbPrice(
    @PrimaryKey @ColumnInfo("asset_id") val assetId: String,
    val value: Double? = 0.0,
    @ColumnInfo("usd_value") val usdValue: Double? = 0.0,
    @ColumnInfo("day_changed") val dayChanged: Double? = 0.0,
    val currency: String,
)

fun AssetPrice.toRecord(rate: FiatRate): DbPrice {
    val currency = Currency.entries.firstOrNull { it.string == rate.symbol } ?: Currency.USD
    return DbPrice(
        assetId = assetId.toIdentifier(),
        value = price * rate.rate,
        usdValue = price,
        dayChanged = priceChangePercentage24h,
        currency = currency.string
    )
}

fun List<AssetPrice>.toRecord(rate: FiatRate) = map { it.toRecord(rate) }