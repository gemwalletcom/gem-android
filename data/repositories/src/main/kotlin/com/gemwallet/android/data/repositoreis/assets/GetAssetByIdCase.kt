package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.entities.toDTO
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import kotlinx.coroutines.flow.firstOrNull

class GetAssetByIdCase(
    private val assetsDao: AssetsDao,
) {
    suspend fun getById(assetId: List<AssetId>): List<Asset> {
        return assetsDao.getAssets(assetId.map { it.toIdentifier() }).firstOrNull()
            ?.toDTO() ?: emptyList()
    }
}