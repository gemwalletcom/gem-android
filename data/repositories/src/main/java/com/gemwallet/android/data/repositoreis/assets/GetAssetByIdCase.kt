package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import kotlinx.coroutines.flow.firstOrNull

class GetAssetByIdCase(
    private val assetsDao: AssetsDao,
) {
    suspend fun getById(assetId: AssetId): Asset? {
        val room = assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain).firstOrNull() ?: return null
        return Asset(
            id = assetId,
            name = room.name,
            symbol = room.symbol,
            decimals = room.decimals,
            type = room.type,
        )
    }
}