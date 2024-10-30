package com.gemwallet.android.data.asset

import com.gemwallet.android.data.database.AssetsDao
import com.gemwallet.android.data.database.BalancesDao
import com.gemwallet.android.data.database.PricesDao
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbBalance
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.AssetPriceInfo
import com.gemwallet.android.model.Balance
import com.gemwallet.android.model.Balances
import com.google.gson.Gson
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAssetByIdCase(
    private val assetsDao: AssetsDao,
) {
    suspend fun getById(assetId: AssetId): Asset? {
        val room = assetsDao.getById(assetId.toIdentifier()).firstOrNull() ?: return null
        return Asset(
            id = assetId,
            name = room.name,
            symbol = room.symbol,
            decimals = room.decimals,
            type = room.type,
        )
    }
}