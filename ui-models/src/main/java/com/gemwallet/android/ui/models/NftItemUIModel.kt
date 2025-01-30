package com.gemwallet.android.ui.models

import com.gemwallet.android.ui.models.actions.NftAssetIdAction
import com.gemwallet.android.ui.models.actions.NftCollectionIdAction
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NFTCollection

data class NftItemUIModel(
    val collection: NFTCollection,
    val asset: NFTAsset? = null,
    val collectionSize: Int? = null,
) {
    val imageUrl: String get() = asset?.image?.imageUrl ?: collection.image.imageUrl
    val name: String get() = asset?.name ?: collection.name

    fun onClick(collectionAction: NftCollectionIdAction, assetAction: NftAssetIdAction) {
        if (asset == null) collectionAction(collection.id) else assetAction(asset.id)
    }
}