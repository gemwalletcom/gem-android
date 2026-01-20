package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.PriceAlertInfo
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "price_alerts")
data class DbPriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: String,
    val currency: String,
    val price: Double? = null,
    val pricePercentChange: Double? = null,
    val priceDirection: PriceAlertDirection? = null,
    val lastNotifiedAt: Long? = null,
    val enabled: Boolean,
)

fun DbPriceAlert.toDTO(): PriceAlertInfo {
    return PriceAlertInfo(
        id = id,
        priceAlert = PriceAlert(
            assetId = assetId.toAssetId() ?: throw IllegalStateException(),
            price = price,
            priceDirection = priceDirection,
            pricePercentChange = pricePercentChange,
            currency = currency,
            lastNotifiedAt = lastNotifiedAt,
        )
    )
}

fun PriceAlert.toRecord(): DbPriceAlert {
    return DbPriceAlert(
        assetId = assetId.toIdentifier(),
        price = price,
        pricePercentChange = pricePercentChange,
        priceDirection = priceDirection,
        enabled = true,
        currency = currency,
        lastNotifiedAt = lastNotifiedAt,
    )
}

fun List<DbPriceAlert>.toDTO() = map { it.toDTO() }

fun Flow<DbPriceAlert>.toDTO() = map { it.toDTO() }

fun List<PriceAlert>.toRecord() = map { it.toRecord() }
