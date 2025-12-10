package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType

@Entity(tableName = "perpetual_asset")
data class DbPerpetualAsset(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
)

fun DbPerpetualAsset.toDTO(): Asset? {
    return Asset(
        id = id.toAssetId() ?: return null,
        name = name,
        symbol = symbol,
        decimals = decimals,
        type = type
    )
}

fun Asset.toDB(): DbPerpetualAsset {
    return DbPerpetualAsset(
        id = id.toIdentifier(),
        name = name,
        symbol = symbol,
        decimals = decimals,
        type = type
    )
}