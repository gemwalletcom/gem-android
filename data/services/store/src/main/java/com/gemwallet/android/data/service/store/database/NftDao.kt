package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gemwallet.android.data.service.store.database.entities.DbNFTAsset
import com.gemwallet.android.data.service.store.database.entities.DbNFTAssociation
import com.gemwallet.android.data.service.store.database.entities.DbNFTAttribute
import com.gemwallet.android.data.service.store.database.entities.DbNFTCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface NftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collection: List<DbNFTCollection>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(assets: List<DbNFTAsset>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetAttributes(attributes: List<DbNFTAttribute>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun associateWithWallet(relations: List<DbNFTAssociation>)

    @Query("""
        DELETE FROM nft_collection WHERE
            id IN (SELECT nft_asset.id FROM nft_asset
                LEFT JOIN nft_association ON nft_asset.id = nft_association.asset_id
                AND nft_association.wallet_id = :walletId
            )
    """)
    suspend fun clean(walletId: String)

    @Transaction
    suspend fun updateNft(
        walletId: String,
        collections: List<DbNFTCollection>,
        assets: List<DbNFTAsset>,
        attributes: List<DbNFTAttribute>,
        associations: List<DbNFTAssociation>,
    ) {
        clean(walletId)
        insertCollections(collections)
        insertAsset(assets)
        insertAssetAttributes(attributes)
        associateWithWallet(associations)
    }

    @Query("""
        SELECT nft_collection.* FROM nft_collection
        LEFT JOIN nft_asset ON nft_collection.id = nft_asset.collection_id
        LEFT JOIN nft_association ON nft_asset.id = nft_association.asset_id
            AND nft_association.wallet_id = (SELECT wallet_id FROM session WHERE id = 1)
    """)
    fun getCollection(): Flow<List<DbNFTCollection>>

    @Query("""
        SELECT nft_asset.* FROM nft_asset
        LEFT JOIN nft_association ON nft_asset.id = nft_association.asset_id
            AND nft_association.wallet_id = (SELECT wallet_id FROM session WHERE id = 1)
    """)
    fun getAssets(): Flow<List<DbNFTAsset>>

    @Query("""
        SELECT *.nft_attribute FROM nft_attribute
        LEFT JOIN nft_asset ON nft_attribute.nft_asset_id = nft_asset.id
        LEFT JOIN nft_association ON nft_association.id = nft_association.nft_asset_id
            AND nft_association.wallet_id = (SELECT wallet_id FROM session WHERE id = 1)
    """)
    fun getAttributes(): Flow<List<DbNFTAttribute>>
}