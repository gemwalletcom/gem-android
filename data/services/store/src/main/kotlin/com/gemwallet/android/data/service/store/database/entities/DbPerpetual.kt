package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Perpetual
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualMetadata
import com.wallet.core.primitives.PerpetualProvider

@Entity(
    tableName = "perpetual",
    indices = [Index(name = "perpetual_asset_id_idx", value = ["assetId"])],
)
data class DbPerpetual(
    @PrimaryKey val id: String,
    val name: String,
    val provider: PerpetualProvider,
    val assetId: String,
    val identifier: String,
    val price: Double,
    val pricePercentChange24h: Double,
    val openInterest: Double,
    val volume24h: Double,
    val funding: Double,
    val maxLeverage: Int,
)


@Entity(tableName = "perpetual_metadata")
data class DbPerpetualMetadata(
    @PrimaryKey val id: String,
    val isPinned: Boolean,
)

data class DbPerpetualData(
    @Embedded val perpetual: DbPerpetual,
    @Embedded val metadata: DbPerpetualMetadata,
    @Embedded val asset: DbAsset,
)

fun DbPerpetual.toDTO(): Perpetual? {
    return Perpetual(
        id = id,
        name = name,
        provider = provider,
        assetId = assetId.toAssetId() ?: return null,
        identifier = identifier,
        price = price,
        pricePercentChange24h = pricePercentChange24h,
        openInterest = openInterest,
        volume24h = volume24h,
        funding = funding,
        maxLeverage = maxLeverage.toUByte(),
    )
}

fun DbPerpetualMetadata.toDTO(): PerpetualMetadata {
    return PerpetualMetadata(isPinned)
}

fun Perpetual.toDB(): DbPerpetual {
    return DbPerpetual(
        id = id,
        name = name,
        provider = provider,
        assetId = assetId.toIdentifier(),
        identifier = identifier,
        price = price,
        pricePercentChange24h = pricePercentChange24h,
        openInterest = openInterest,
        volume24h = volume24h,
        funding = funding,
        maxLeverage = maxLeverage.toInt(),
    )
}

fun PerpetualMetadata.toDB(perpetualId: String): DbPerpetualMetadata {
    return DbPerpetualMetadata(perpetualId, isPinned)
}

fun DbPerpetualData.toDTO(): PerpetualData? {
    return PerpetualData(
        perpetual = perpetual.toDTO() ?: return null,
        asset = asset.toDTO() ?: return null,
        metadata = metadata.toDTO(),
    )
}