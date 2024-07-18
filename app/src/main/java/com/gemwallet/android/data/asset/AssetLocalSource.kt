package com.gemwallet.android.data.asset

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetPrice
import kotlinx.coroutines.flow.Flow

interface AssetsLocalSource {

    suspend fun getNativeAssets(accounts: List<Account>): Result<List<Asset>>

    suspend fun getAllAssetsIds(): List<AssetId>

    suspend fun getAssetsInfo(accounts: List<Account>): List<AssetInfo>

    suspend fun search(query: String): Flow<List<AssetInfo>>

    fun getAssetsInfo(): Flow<List<AssetInfo>>

    fun getAssetsInfo(ids: List<AssetId>): Flow<List<AssetInfo>>

    fun getAssetInfo(assetId: AssetId): Flow<AssetInfo?>

    suspend fun getById(accounts: List<Account>, assetId: AssetId): Result<List<AssetInfo>>

    suspend fun getById(accounts: List<Account>, ids: List<AssetId>): Result<List<AssetInfo>>

    suspend fun add(address: String, asset: Asset, visible: Boolean)

    suspend fun add(address: String, assets: List<Asset>)

    suspend fun setBalances(account: Account, balances: List<Balances>)

    suspend fun setVisibility(account: Account, assetId: AssetId, visibility: Boolean)

    suspend fun setPrices(prices: List<AssetPrice>)

    suspend fun clearPrices()

    suspend fun setAssetDetails(
        assetId: AssetId,
        buyable: Boolean,
        swapable: Boolean,
        stakeable: Boolean,
        stakingApr: Double?,
        links: AssetLinks?,
        market: AssetMarket?,
        rank: Int,
    )

    suspend fun getById(assetId: AssetId): Asset?

    suspend fun getStakingApr(assetId: AssetId): Double?
}