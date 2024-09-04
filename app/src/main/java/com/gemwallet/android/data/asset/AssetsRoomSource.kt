package com.gemwallet.android.data.asset

import com.gemwallet.android.data.database.AssetsDao
import com.gemwallet.android.data.database.BalancesDao
import com.gemwallet.android.data.database.PricesDao
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbBalance
import com.gemwallet.android.data.database.entities.DbPrice
import com.gemwallet.android.data.database.mappers.AssetInfoMapper
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AssetsRoomSource @Inject constructor(
    private val assetsDao: AssetsDao,
    private val balancesDao: BalancesDao,
    private val pricesDao: PricesDao,
) : AssetsLocalSource {

    private val gson = Gson()

    override suspend fun getNativeAssets(accounts: List<Account>): Result<List<Asset>> { // For check accounts
        val assets = assetsDao.getAssetsByType(accounts.map { it.address }).mapNotNull {
            Asset(
                id = it.id.toAssetId() ?: return@mapNotNull null,
                name = it.name,
                symbol = it.symbol,
                decimals = it.decimals,
                type = it.type,
            )
        }
        return Result.success(assets)
    }

    override suspend fun getAllAssetsIds(): List<AssetId> { // For update price
        return assetsDao.getAll().map { it.id }.toSet().mapNotNull { it.toAssetId() }.toList()
    }

    override fun getAssetsInfo(): Flow<List<AssetInfo>> = assetsDao.getAssetsInfo()
        .map { AssetInfoMapper().asDomain(it) }

    override fun getAssetsInfo(ids: List<AssetId>): Flow<List<AssetInfo>> = assetsDao
        .getAssetsInfo(ids.map { it.toIdentifier() })
        .map { AssetInfoMapper().asDomain(it) }

    override suspend fun getAssetsInfo(accounts: List<Account>): List<AssetInfo> = withContext(Dispatchers.IO) {
        assetsDao.getAssetsInfoByAccounts(accounts.map { it.address })
            .map { AssetInfoMapper().asDomain(it) }
            .firstOrNull() ?: emptyList()
    }

    override suspend fun search(query: String): Flow<List<AssetInfo>> {
        return assetsDao.searchAssetInfo(query).map { AssetInfoMapper().asDomain(it) }
    }

    override suspend fun getById(accounts: List<Account>, assetId: AssetId): Result<List<AssetInfo>> {
        return getById(accounts, listOf(assetId))
    }

    override suspend fun getById(accounts: List<Account>, ids: List<AssetId>): Result<List<AssetInfo>> = withContext(Dispatchers.IO) {
        val roomAssetId = ids.map { it.toIdentifier() }
        val addresses = accounts.map { it.address }.toSet().toList()
        val assets = assetsDao.getById(addresses, roomAssetId)
        if (assets.isEmpty()) {
            return@withContext Result.failure(Exception("Asset doesn't found"))
        }
        val balances = balancesDao.getByAssetId(addresses, roomAssetId)
        val prices = pricesDao.getByAssets(assets.map { it.id }).map {
            AssetPriceInfo(price = AssetPrice(it.assetId, it.value, it.dayChanged), currency = Currency.USD)
        }
        Result.success(
            assets.mapNotNull { asset ->
                val assetId = asset.id.toAssetId() ?: return@mapNotNull null
                val account = accounts.firstOrNull { it.address == asset.address && it.chain == assetId.chain }
                    ?: return@mapNotNull null
                roomToModel(gson, assetId, account, asset, balances, prices)
            }
        )
    }

    override fun getAssetInfo(assetId: AssetId): Flow<AssetInfo?> {
        val id = assetId.toIdentifier()
        return assetsDao.getAssetInfo(id, assetId.chain)
            .map { AssetInfoMapper().asDomain(it).firstOrNull() }
    }

    override suspend fun getById(assetId: AssetId): Asset? {
        val room = assetsDao.getById(assetId.toIdentifier()).firstOrNull() ?: return null
        return Asset(
            id = assetId,
            name = room.name,
            symbol = room.symbol,
            decimals = room.decimals,
            type = room.type,
        )
    }

    override suspend fun add(address: String, asset: Asset, visible: Boolean) = withContext(Dispatchers.IO) {
        assetsDao.insert(modelToRoom(address, asset).copy(isVisible = visible))
    }

    override suspend fun add(address: String, assets: List<Asset>) {
        assetsDao.insert(assets.map{ modelToRoom(address, it)})
    }

    override suspend fun setBalances(account: Account, balances: List<Balances>) = withContext(Dispatchers.IO) {
        val updatedAt = System.currentTimeMillis()
        balancesDao.insert(
            balances.map { it.items }.flatten().map {
                DbBalance(
                    assetId = it.assetId.toIdentifier(),
                    address = account.address,
                    type = it.balance.type,
                    amount = it.balance.value,
                    updatedAt = updatedAt,
                )
            }
        )
    }

    override suspend fun setVisibility(account: Account, assetId: AssetId, visibility: Boolean) = withContext(Dispatchers.IO) {
        val asset = assetsDao.getById(listOf(account.address), listOf(assetId.toIdentifier())).firstOrNull() ?: return@withContext
        assetsDao.update(asset.copy(isVisible = visibility, isPinned = false))
    }

    override suspend fun togglePinned(account: Account, assetId: AssetId)  = withContext(Dispatchers.IO) {
        val asset = assetsDao.getById(listOf(account.address), listOf(assetId.toIdentifier())).firstOrNull() ?: return@withContext
        assetsDao.update(asset.copy(isPinned = !asset.isPinned))
    }

    override suspend fun setPrices(prices: List<AssetPrice>) = withContext(Dispatchers.IO) {
        pricesDao.insert(
            prices.map {
                price -> DbPrice(price.assetId, price.price, price.priceChangePercentage24h)
            }
        )
    }

    override suspend fun clearPrices() {
        pricesDao.deleteAll()
    }

    override suspend fun setAssetDetails(
        assetId: AssetId,
        buyable: Boolean,
        swapable: Boolean,
        stakeable: Boolean,
        stakingApr: Double?,
        links: AssetLinks?,
        market: AssetMarket?,
        rank: Int,
    ) {
        val gson = Gson()
        assetsDao.getById(assetId.toIdentifier()).map {
            it.copy(
                isBuyEnabled = buyable,
                isSwapEnabled = swapable,
                isStakeEnabled = stakeable,
                stakingApr = stakingApr,
                links = if (links == null) null else gson.toJson(links),
                market = if (market == null) null else gson.toJson(market),
                rank = rank,
            )
        }.forEach {
            assetsDao.update(it)
        }
    }

    override suspend fun getStakingApr(assetId: AssetId): Double? {
        return assetsDao.getById(assetId.toIdentifier()).firstOrNull()?.stakingApr
    }

    private fun modelToRoom(address: String, asset: Asset) = DbAsset(
        id = asset.id.toIdentifier(),
        address = address,
        name = asset.name,
        symbol = asset.symbol,
        decimals = asset.decimals,
        type = asset.type,
        createdAt = System.currentTimeMillis(),
    )

    private fun roomToModel(
        gson: Gson,
        assetId: AssetId,
        account: Account,
        room: DbAsset,
        balances: List<DbBalance>,
        prices: List<AssetPriceInfo>
    ) = AssetInfo(
        owner = account,
        asset = Asset(
            id = assetId,
            name = room.name,
            symbol = room.symbol,
            decimals = room.decimals,
            type = room.type,
        ),
        balances = Balances(
            balances
                .filter { it.assetId == room.id && it.address == room.address }
                .map { AssetBalance(assetId, Balance(value = it.amount, type = it.type)) }
        ),
        price = prices.firstOrNull { it.price.assetId ==  room.id},
        metadata = AssetMetaData(
            isEnabled = room.isVisible,
            isBuyEnabled = room.isBuyEnabled,
            isSwapEnabled = room.isSwapEnabled,
            isStakeEnabled = room.isStakeEnabled,
            isPinned = false,
        ),
        links = if (room.links != null) gson.fromJson(room.links, AssetLinks::class.java) else null,
        market = if (room.market != null) gson.fromJson(room.market, AssetMarket::class.java) else null,
        rank = room.rank,
    )
}