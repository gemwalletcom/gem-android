package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

fun DbPriceAlert.toModel(): PriceAlert {
    return PriceAlert(
        assetId = assetId.toAssetId() ?: throw IllegalStateException(),
        price = price,
        priceDirection = priceDirection,
        pricePercentChange = pricePercentChange,
        currency = Currency.USD.string, // TODO: Add user selected
    )
}

fun PriceAlert.toRecord(): DbPriceAlert {
    return DbPriceAlert(
        assetId = assetId.toIdentifier(),
        price = price,
        pricePercentChange = pricePercentChange,
        priceDirection = priceDirection,
        enabled = true,
    )
}

fun List<DbPriceAlert>.toModel() = map { it.toModel() }

fun Flow<List<DbPriceAlert>>.toModels() = map { it.toModel() }

fun Flow<DbPriceAlert>.toModel() = map { it.toModel() }

fun List<PriceAlert>.toRecord() = map { it.toRecord() }
