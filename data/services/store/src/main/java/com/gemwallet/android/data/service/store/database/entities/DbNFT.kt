package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NFTType
import kotlinx.serialization.Serializable

@Entity(tableName = "nft_collection")
data class DbNFTCollection(
    @PrimaryKey val id: String,
    @PrimaryKey val name: String,
    val description: String? = null,
    val chain: Chain,
    @ColumnInfo(name = "contract_address") val contractAddress: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "preview_image_url") val previewImageUrl: String,
    @ColumnInfo(name = "original_image_url") val originalSourceUrl: String,
    @ColumnInfo(name = "is_verified") val isVerified: Boolean,
)

@Entity(tableName = "nft_asset", primaryKeys = ["owner_address", "id"])
data class DbNFTAsset(
    @PrimaryKey val id: String,
    @ColumnInfo("collection_id") val collectionId: String,
    @ColumnInfo("token_id") val tokenId: String,
    @ColumnInfo("token_type") val tokenType: NFTType,
    val name: String,
    val description: String? = null,
    val chain: Chain,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "preview_image_url") val previewImageUrl: String,
    @ColumnInfo(name = "original_image_url") val originalSourceUrl: String,
)

@Entity(tableName = "nft_attributes")
data class DbNFTAttribute (
    @ColumnInfo("nft_asset_id") val assetId: String,
    val name: String,
    val value: String
)

@Entity(tableName = "nft_association")
data class DbNFTAssociation(
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("asset_id") val assetId: String,
)