package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetBasic

@Entity(
    tableName = "assets_priority",
    primaryKeys = ["query", "asset_id"],
)
data class DbAssetPriority(
    val query: String,
    @ColumnInfo(name = "asset_id") val assetId: String,
    val priority: Int,
)

fun AssetBasic.toRecordPriority(query: String): DbAssetPriority {
    return DbAssetPriority(
        query = query,
        assetId = asset.id.toIdentifier(),
        priority = score.rank,
    )
}

fun List<AssetBasic>.toRecordPriority(query: String) = map { it.toRecordPriority(query) }