package com.gemwallet.android.data.repositoreis.nft

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.nft.GetAssetNftCase
import com.gemwallet.android.cases.nft.GetListNftCase
import com.gemwallet.android.cases.nft.LoadNFTCase
import com.gemwallet.android.cases.nft.NftError
import com.gemwallet.android.data.service.store.database.NftDao
import com.gemwallet.android.data.service.store.database.entities.DbNFTAsset
import com.gemwallet.android.data.service.store.database.entities.DbNFTAssociation
import com.gemwallet.android.data.service.store.database.entities.DbNFTAttribute
import com.gemwallet.android.data.service.store.database.entities.DbNFTCollection
import com.gemwallet.android.data.service.store.database.entities.DbNFTCollectionLink
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NFTAttribute
import com.wallet.core.primitives.NFTCollection
import com.wallet.core.primitives.NFTData
import com.wallet.core.primitives.NFTImage
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class NftRepository(
    private val gemApiClient: GemApiClient,
    private val getDeviceId: GetDeviceIdCase,
    private val nftDao: NftDao,
) : LoadNFTCase, GetListNftCase, GetAssetNftCase {

    override suspend fun loadNFT(wallet: Wallet) {
        val deviceId = getDeviceId.getDeviceId()

        val response = gemApiClient.getNFTs(deviceId, wallet.index)
        val data = response.getOrThrow().data

        val collections = data.map {
            DbNFTCollection(
                id = it.collection.id,
                name = it.collection.name,
                description = it.collection.description,
                chain = it.collection.chain,
                contractAddress = it.collection.contractAddress,
                imageUrl = it.collection.image.imageUrl,
                previewImageUrl = it.collection.image.previewImageUrl,
                originalSourceUrl = it.collection.image.previewImageUrl,
                isVerified = it.collection.isVerified,
            )
        }
        val fullAsset = data.map { item ->
            item.assets.map { asset ->
                Pair(
                    DbNFTAsset(
                        id = asset.id,
                        collectionId = item.collection.id,
                        name = asset.name,
                        tokenId = asset.tokenId,
                        tokenType = asset.tokenType,
                        chain = asset.chain,
                        description = asset.description,
                        imageUrl = asset.image.imageUrl,
                        previewImageUrl = asset.image.previewImageUrl,
                        originalSourceUrl = asset.image.previewImageUrl,
                    ),
                    asset.attributes.map {
                        DbNFTAttribute(
                            asset.id,
                            it.name,
                            it.value
                        )
                    }
                )
            }
        }.flatten()
        val assets = fullAsset.map { it.first }
        val attributes = fullAsset.map { it.second }.flatten()
        val associations = assets.map {
            DbNFTAssociation(
                wallet.id,
                it.id
            )
        }
        val links = data.map { it.collection }
            .map { it.links.map { link -> DbNFTCollectionLink(it.id, link.name, link.url) } }
            .flatten()
        nftDao.updateNft(
            wallet.id,
            collections,
            links,
            assets,
            attributes,
            associations,
        )
    }

    override fun getListNft(collectionId: String?): Flow<List<NFTData>> {
        return combine(
            nftDao.getCollection(),
            nftDao.getAssets(),
            nftDao.getAttributes()
        ) { collectionEntities, assetEntities, attributeEntities ->
            val assets = assetEntities.mapToModel(attributeEntities).groupBy { it.collectionId }
            val collections = collectionEntities.mapToModel()
            collections.map { NFTData(it, assets[it.id] ?: emptyList()) }
                .filter { it.collection.id == (collectionId ?: return@filter true) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Throws(NftError::class)
    override fun getAssetNft(id: String): Flow<NFTData> = nftDao.getAsset(id).flatMapLatest { assetEntity ->
        assetEntity ?: throw NftError.NotFoundAsset

        val collectionFlow = combine(
            nftDao.getCollection(assetEntity.collectionId),
            nftDao.getCollectionLinks(assetEntity.collectionId),
        ) { collection, links ->
            collection?.mapToModel(links)
        }

        combine(nftDao.getAttributes(assetEntity.id), collectionFlow) { attrs, collection ->
            collection ?: throw NftError.NotFoundCollection

            NFTData(
                collection = collection,
                assets = listOf(assetEntity.mapToModel(attrs))
            )
        }

    }
}

private fun List<DbNFTCollection>.mapToModel() = map { it.mapToModel(emptyList()) }

private fun DbNFTCollection.mapToModel(links: List<DbNFTCollectionLink>) = NFTCollection(
    id = this.id,
    name = this.name,
    description = this.description,
    chain = this.chain,
    contractAddress = this.contractAddress,
    image = NFTImage(this.imageUrl, this.previewImageUrl, this.originalSourceUrl),
    isVerified = this.isVerified,
    links = links.map { AssetLink(it.name, it.url) },
)

private fun List<DbNFTAsset>.mapToModel(attributes: List<DbNFTAttribute>): List<NFTAsset> {
    val attrIndex = attributes.groupBy { it.assetId }
    return map { entity ->
        val assetAttributes = attrIndex[entity.id]
        entity.mapToModel(assetAttributes)
    }
}

private fun DbNFTAsset.mapToModel(attributes: List<DbNFTAttribute>?) = NFTAsset(
    id = this.id,
    collectionId = this.collectionId,
    tokenId = this.tokenId,
    tokenType = this.tokenType,
    name = this.name,
    description = this.description,
    chain = this.chain,
    image = NFTImage(
        imageUrl = this.imageUrl,
        previewImageUrl = this.previewImageUrl,
        originalSourceUrl = this.originalSourceUrl,
    ),
    attributes = attributes?.map { NFTAttribute(it.name, it.value) } ?: emptyList(),
)