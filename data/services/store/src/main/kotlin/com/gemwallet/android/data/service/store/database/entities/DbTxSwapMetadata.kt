package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tx_swap_metadata")
data class DbTxSwapMetadata(
    @PrimaryKey @ColumnInfo(name = "tx_id") val txId: String,
    @ColumnInfo(name = "from_asset_id") val fromAssetId: String,
    @ColumnInfo(name = "to_asset_id") val toAssetId: String,
    @ColumnInfo(name = "from_amount") val fromAmount: String,
    @ColumnInfo(name = "to_amount") val toAmount: String,
)