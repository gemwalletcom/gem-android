package com.gemwallet.android.data.asset

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
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
import com.wallet.core.primitives.BalanceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "assets", primaryKeys = ["owner_address", "id"])
data class AssetRoom(
    @ColumnInfo("owner_address", index = true) val address: String,
    @ColumnInfo(index = true) val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    @ColumnInfo("is_visible") val isVisible: Boolean = true,
    @ColumnInfo("is_buy_enabled") val isBuyEnabled: Boolean = false,
    @ColumnInfo("is_swap_enabled") val isSwapEnabled: Boolean = false,
    @ColumnInfo("is_stake_enabled") val isStakeEnabled: Boolean = false,
    @ColumnInfo("staking_apr") val stakingApr: Double? = null,
    @ColumnInfo("links") val links: String? = null,
    @ColumnInfo("market") val market: String? = null,
    @ColumnInfo("rank") val rank: Int = 0,
    @ColumnInfo("created_at") val createdAt: Long = 0,
    @ColumnInfo("updated_at") val updatedAt: Long = 0,
)

@Entity(tableName = "balances", primaryKeys = ["asset_id", "address", "type"])
data class BalanceRoom(
    @ColumnInfo("asset_id", index = true) val assetId: String,
    @ColumnInfo(index = true) val address: String,
    val type: BalanceType,
    val amount: String,
    @ColumnInfo("updated_at") val updatedAt: Long,
)

@Entity(tableName = "prices")
data class PriceRoom(
    @PrimaryKey val assetId: String,
    val value: Double,
    val dayChanged: Double,
)

@Dao
interface AssetsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(asset: AssetRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(asset: List<AssetRoom>)

    @Update
    fun update(asset: AssetRoom)

    @Query("SELECT * FROM assets")
    fun getAll(): List<AssetRoom>

    @Query("SELECT * FROM assets " +
            "WHERE owner_address IN (:addresses) " +
            "AND (id LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%') " +
            "COLLATE NOCASE")
    fun getAllByOwner(addresses: List<String>, query: String): Flow<List<AssetRoom>>

    @Query("SELECT DISTINCT * FROM assets WHERE owner_address IN (:addresses) AND id IN (:assetId)")
    fun getById(addresses: List<String>, assetId: List<String>): List<AssetRoom>

    @Query("SELECT DISTINCT * FROM assets WHERE owner_address = :address AND id = :assetId")
    fun getById(address: String, assetId: String): Flow<AssetRoom?>

    @Query("SELECT DISTINCT * FROM assets WHERE id = :assetId")
    suspend fun getById(assetId: String): List<AssetRoom>

    @Query("SELECT * FROM assets WHERE owner_address IN (:addresses) AND type = :type")
    fun getAssetsByType(addresses: List<String>, type: AssetType = AssetType.NATIVE): List<AssetRoom>
}

@Dao
interface BalancesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: BalanceRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: List<BalanceRoom>)

    @Update
    fun update(balance: BalanceRoom)

    @Query("SELECT * FROM balances WHERE address IN (:addresses)")
    fun getAllByOwner(addresses: List<String>): Flow<List<BalanceRoom>>

    @Query("SELECT * FROM balances WHERE address IN (:addresses) AND asset_id IN (:assetId)")
    fun getByAssetId(addresses: List<String>, assetId: List<String>): List<BalanceRoom>

    @Query("SELECT * FROM balances WHERE address = :address AND asset_id = :assetId")
    fun getByAssetId(address: String, assetId: String): Flow<List<BalanceRoom>>
}

@Dao
interface PricesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(priceRoom: PriceRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(priceRoom: List<PriceRoom>)

    @Query("SELECT * FROM prices")
    fun getAll(): Flow<List<PriceRoom>>

    @Query("SELECT * FROM prices WHERE assetId IN (:assetsId)")
    fun getByAssets(assetsId: List<String>): List<PriceRoom>

    @Query("SELECT * FROM prices WHERE assetId = :assetsId")
    fun getByAssets(assetsId: String): Flow<PriceRoom>

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
                AssetPrice(it.assetId, it.value, it.dayChanged)
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
            AssetPrice(it.assetId, it.value, it.dayChanged)
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

    override suspend fun getAssetInfo(account: Account, assetId: AssetId): Flow<AssetInfo?> {
        val roomAssetId = assetId.toIdentifier()
        val address = account.address
        return assetsDao.getById(address, roomAssetId).combine(
            flow = balancesDao.getByAssetId(address, roomAssetId)
        ) { asset, balances ->
            if (asset == null) {
                return@combine null
            }
            roomToModel(gson, assetId, account, asset, balances, emptyList())
        }.combine(pricesDao.getByAssets(roomAssetId)) { assetInfo, prices ->
            assetInfo?.copy(price = AssetPrice(prices.assetId, prices.value, prices.dayChanged)) ?: assetInfo
        }.filterNotNull()
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
                BalanceRoom(
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
                price -> PriceRoom(price.assetId, price.price, price.priceChangePercentage24h)
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

    private fun modelToRoom(address: String, asset: Asset) = AssetRoom(
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
        room: AssetRoom,
        balances: List<BalanceRoom>,
        prices: List<AssetPrice>
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
        price = prices.firstOrNull { it.assetId ==  room.id},
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