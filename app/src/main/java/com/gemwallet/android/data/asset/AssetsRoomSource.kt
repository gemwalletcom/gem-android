package com.gemwallet.android.data.asset

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbAssetInfo
import com.gemwallet.android.data.database.entities.DbBalance
import com.gemwallet.android.data.database.entities.DbPrice
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
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Dao
interface AssetsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(asset: DbAsset)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(asset: List<DbAsset>)

    @Update
    fun update(asset: DbAsset)

    @Query("SELECT * FROM assets")
    fun getAll(): List<DbAsset>

    @Query("SELECT * FROM assets " +
            "WHERE owner_address IN (:addresses) " +
            "AND (id LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%') " +
            "COLLATE NOCASE")
    fun getAllByOwner(addresses: List<String>, query: String): Flow<List<DbAsset>>

    @Query("SELECT DISTINCT * FROM assets WHERE owner_address IN (:addresses) AND id IN (:assetId)")
    fun getById(addresses: List<String>, assetId: List<String>): List<DbAsset>

    @Query("SELECT DISTINCT * FROM assets WHERE id = :assetId")
    suspend fun getById(assetId: String): List<DbAsset>

    @Query("SELECT * FROM assets WHERE owner_address IN (:addresses) AND type = :type")
    fun getAssetsByType(addresses: List<String>, type: AssetType = AssetType.NATIVE): List<DbAsset>

    @Query("""
        SELECT
            assets.*,
            accounts.*,
            session.currency AS priceCurrency,
            wallets.type AS walletType,
            wallets.name AS walletName,
            prices.value AS priceValue,
            prices.dayChanged AS priceDayChanges,
            balances.amount AS amount,
            balances.type as balanceType
        FROM assets
        JOIN accounts ON accounts.address = assets.owner_address
        JOIN wallets ON wallets.id = accounts.wallet_id
        JOIN session ON accounts.wallet_id = session.wallet_id
        LEFT JOIN balances ON assets.owner_address = balances.address AND assets.id = balances.asset_id
        LEFT JOIN prices ON assets.id = prices.assetId
        WHERE accounts.chain = :chain AND assets.id = :assetId
        """)
    fun getAssetById(assetId: String, chain: Chain): Flow<List<DbAssetInfo>>
}

@Dao
interface BalancesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: DbBalance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: List<DbBalance>)

    @Update
    fun update(balance: DbBalance)

    @Query("SELECT * FROM balances WHERE address IN (:addresses)")
    fun getAllByOwner(addresses: List<String>): Flow<List<DbBalance>>

    @Query("SELECT * FROM balances WHERE address IN (:addresses) AND asset_id IN (:assetId)")
    fun getByAssetId(addresses: List<String>, assetId: List<String>): List<DbBalance>
}

@Dao
interface PricesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(priceRoom: DbPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(priceRoom: List<DbPrice>)

    @Query("SELECT * FROM prices")
    fun getAll(): Flow<List<DbPrice>>

    @Query("SELECT * FROM prices WHERE assetId IN (:assetsId)")
    fun getByAssets(assetsId: List<String>): List<DbPrice>

    @Query("DELETE FROM prices")
    fun deleteAll()
}


@Singleton
class AssetsRoomSource @Inject constructor(
    private val assetsDao: AssetsDao,
    private val balancesDao: BalancesDao,
    private val pricesDao: PricesDao,
) : AssetsLocalSource {

    private val gson = Gson()

    override suspend fun getNativeAssets(accounts: List<Account>): Result<List<Asset>> {
        val assets = assetsDao.getAssetsByType(accounts.map { it.address })
            .mapNotNull {
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

    override suspend fun getAllAssetsIds(): List<AssetId> {
        return assetsDao.getAll().map { it.id }.toSet().mapNotNull { it.toAssetId() }.toList()
    }

    override suspend fun getAllByAccount(account: Account): Result<List<AssetInfo>> {
        return getAllByAccounts(listOf(account))
    }

    override suspend fun getAllByAccounts(accounts: List<Account>): Result<List<AssetInfo>> = withContext(Dispatchers.IO) {
        Result.success(getAllByAccounts(accounts, "").firstOrNull() ?: emptyList())
    }

    override suspend fun getAllByAccountsFlow(accounts: List<Account>): Flow<List<AssetInfo>> = withContext(Dispatchers.IO) {
        getAllByAccounts(accounts, "")
    }

    private fun getAllByAccounts(accounts: List<Account>, query: String): Flow<List<AssetInfo>> {
        val addresses = accounts.map { it.address }.toSet().toList()
        val chains = accounts.map { it.chain }
        val assetsFlow = assetsDao.getAllByOwner(addresses, query)
        val balancesFlow = balancesDao.getAllByOwner(addresses)
        val pricesFlow = pricesDao.getAll()

        return combine(assetsFlow, balancesFlow, pricesFlow) { assets, balances, allPrices ->
            val prices = allPrices.map {
                AssetPriceInfo(price = AssetPrice(it.assetId, it.value, it.dayChanged), currency = Currency.USD)
            }
            assets.mapNotNull { asset ->
                val assetId = asset.id.toAssetId() ?: return@mapNotNull null
                if (!chains.contains(assetId.chain)) {
                    return@mapNotNull null
                }
                val account = accounts.firstOrNull { it.address == asset.address && it.chain == assetId.chain }
                    ?: return@mapNotNull null
                roomToModel(gson, assetId, account, asset, balances, prices)
            }
        }
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

    override suspend fun getAssetInfo(assetId: AssetId): Flow<AssetInfo?> {
        val id = assetId.toIdentifier()
        return assetsDao.getAssetById(id, assetId.chain).map { it.asDomain(gson).firstOrNull() }
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

    override suspend fun getAssets(account: Account): Result<List<Asset>> {
        val result = assetsDao.getAllByOwner(listOf(account.address), "")
            .firstOrNull()
            ?.mapNotNull {
                val assetId = it.id.toAssetId() ?: return@mapNotNull null
                Asset(
                    id = assetId,
                    name = it.name,
                    symbol = it.symbol,
                    decimals = it.decimals,
                    type = it.type,
                )
            }
            ?.filter { it.id.chain == account.chain} ?: emptyList()
        return Result.success(result)
    }

    override suspend fun add(address: String, asset: Asset) = withContext(Dispatchers.IO) {
        assetsDao.insert(modelToRoom(address, asset))
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
        assetsDao.update(asset.copy(isVisible = visibility))
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

    override suspend fun search(accounts: List<Account>, query: String): Flow<List<AssetInfo>> {
        return getAllByAccounts(accounts, query)
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
        ),
        links = if (room.links != null) gson.fromJson(room.links, AssetLinks::class.java) else null,
        market = if (room.market != null) gson.fromJson(room.market, AssetMarket::class.java) else null,
        rank = room.rank,
    )
}

fun List<DbAssetInfo>.asDomain(
    gson: Gson,
): List<AssetInfo> {
    return groupBy { it.id }.mapNotNull { records ->
        val first = records.value.firstOrNull() ?: return@mapNotNull null
        val assetId = first.id.toAssetId() ?: return@mapNotNull null

        val balances = Balances(
            records.value.mapNotNull {
                if (it.amount != null && it.balanceType != null) {
                    AssetBalance(assetId, Balance(it.balanceType, it.amount))
                } else {
                    null
                }
            }
        )

        val currency = Currency.entries.firstOrNull { it.string == first.priceCurrency }

        AssetInfo(
            owner = Account(
                chain = first.chain,
                address = first.address,
                derivationPath = first.derivationPath,
                extendedPublicKey = first.extendedPublicKey,
            ),
            asset = Asset(
                id = assetId,
                name = first.name,
                symbol = first.symbol,
                decimals = first.decimals,
                type = first.type,
            ),
            balances = balances,
            price = if (first.priceValue != null && currency != null) {
                AssetPriceInfo(
                    currency = currency,
                    price = AssetPrice(
                        assetId = assetId.toIdentifier(),
                        price = first.priceValue,
                        priceChangePercentage24h = first.priceDayChanges ?: 0.0,
                    )
                )
            } else null,
            metadata = AssetMetaData(
                isEnabled = first.isVisible,
                isBuyEnabled = first.isBuyEnabled,
                isSwapEnabled = first.isSwapEnabled,
                isStakeEnabled = first.isStakeEnabled,
            ),
            links = if (first.links != null) gson.fromJson(
                first.links,
                AssetLinks::class.java
            ) else null,
            market = if (first.market != null) gson.fromJson(
                first.market,
                AssetMarket::class.java
            ) else null,
            rank = first.rank,
            walletName = first.walletName,
            walletType = first.walletType,
            stakeApr = first.stakingApr,
        )
    }
}