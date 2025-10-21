package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.service.store.database.entities.DbAsset
import com.gemwallet.android.data.service.store.database.entities.DbAssetConfig
import com.gemwallet.android.data.service.store.database.entities.DbAssetInfo
import com.gemwallet.android.data.service.store.database.entities.DbAssetLink
import com.gemwallet.android.data.service.store.database.entities.DbAssetMarket
import com.gemwallet.android.data.service.store.database.entities.DbAssetWallet
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: DbAsset)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(asset: List<DbAsset>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(asset: DbAsset, walletLink: DbAssetWallet, config: DbAssetConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(links: List<DbAssetLink>, market: DbAssetMarket)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLinks(links: List<DbAssetLink>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: DbAssetConfig)

    @Update
    fun update(asset: DbAsset)

    @Query("UPDATE asset SET is_swap_enabled=1 WHERE chain IN (:chains)")
    suspend fun setSwapable(chains: List<Chain>)

    @Query("UPDATE asset SET is_swap_enabled=0")
    suspend fun resetSwapable()

    @Query("UPDATE asset SET is_buy_enabled=0")
    suspend fun resetBuyAvailable()

    @Query("UPDATE asset SET is_buy_enabled=1 WHERE id IN (:ids)")
    suspend fun updateBuyAvailable(ids: List<String>)

    @Query("UPDATE asset SET is_sell_enabled=0")
    suspend fun resetSellAvailable()

    @Query("UPDATE asset SET is_sell_enabled=1 WHERE id IN (:ids)")
    suspend fun updateSellAvailable(ids: List<String>)

    @Query("SELECT * FROM asset")
    suspend fun getAll(): List<DbAsset>

    @Query("SELECT asset.* FROM asset JOIN asset_wallet ON asset.id = asset_wallet.asset_id WHERE wallet_id = :walletId")
    fun getNativeWalletAssets(walletId: String): Flow<List<DbAsset>>

    @Query("SELECT * FROM asset_info WHERE chain = :chain AND id = :assetId AND sessionId = 1")
    fun getAssetInfo(assetId: String, chain: Chain): Flow<DbAssetInfo?>

    @Query("SELECT * FROM asset_info WHERE chain = :chain AND id = :assetId")
    fun getTokenInfo(assetId: String, chain: Chain): Flow<DbAssetInfo?>

    @Query("SELECT DISTINCT * FROM asset_info WHERE sessionId = 1 AND visible != 0")
    fun getAssetsInfo(): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE id IN (:ids) AND sessionId=1 ORDER BY balanceFiatTotalAmount DESC")
    fun getAssetsInfo(ids: List<String>): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE id IN (:ids) ORDER BY balanceFiatTotalAmount DESC")
    fun getAssetsInfoByAllWallets(ids: List<String>): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT DISTINCT asset.id FROM asset
        JOIN asset_config ON asset_config.asset_id = asset.id
        WHERE asset_config.is_visible = 1
    """)
    suspend fun getAssetsPriceUpdate(): List<String>

    @Query("""
        SELECT
            *,
            MAX(address)
        FROM asset_info WHERE
            id NOT IN (:exclude)
            AND (chain IN (SELECT chain FROM accounts JOIN session ON accounts.wallet_id = session.wallet_id AND session.id = 1))
            AND (walletId = (SELECT wallet_id FROM session WHERE session.id = 1) OR walletId IS NULL)
            AND (symbol LIKE '%' || :query || '%'
            OR name LIKE '%' || :query || '%' COLLATE NOCASE)
            GROUP BY id
            ORDER BY balanceFiatTotalAmount DESC, assetRank DESC
        """)
    fun search(query: String, exclude: List<String> = emptyList()): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT
            *,
            assets_priority.priority,
            MAX(address)
        FROM asset_info
        JOIN assets_priority ON id IN (assets_priority.asset_id)
        WHERE
            id NOT IN (:exclude)
            AND (chain IN (SELECT chain FROM accounts JOIN session ON accounts.wallet_id = session.wallet_id AND session.id = 1))
            AND (walletId = (SELECT wallet_id FROM session WHERE session.id = 1) OR walletId IS NULL)
            AND `query` = :query
            GROUP BY id
            ORDER BY balanceFiatTotalAmount DESC, priority DESC, assetRank DESC
        """)
    fun searchWithPriority(query: String, exclude: List<String> = emptyList()): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT
            *,
            MAX(address)
        FROM asset_info WHERE
            (symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' COLLATE NOCASE)
            GROUP BY id
            ORDER BY balanceFiatTotalAmount DESC, assetRank DESC
            
        """)
    fun searchByAllWallets(query: String): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT
            *,
            assets_priority.priority,
            MAX(address)
        FROM asset_info
        JOIN assets_priority ON id IN (assets_priority.asset_id)
        WHERE
            `query` = :query
            GROUP BY id
            ORDER BY balanceFiatTotalAmount DESC, priority DESC, assetRank DESC
            
        """)
    fun searchByAllWalletsWithPriority(query: String): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT
            *,
            MAX(address)
        FROM asset_info WHERE
            (chain IN (:byChains) OR id IN (:byAssets) )
            AND (symbol LIKE '%' || :query || '%'
            OR name LIKE '%' || :query || '%' COLLATE NOCASE)
            GROUP BY id
            ORDER BY balanceFiatTotalAmount DESC, assetRank DESC
        """)
    fun swapSearch(query: String, byChains: List<Chain>, byAssets: List<String>): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_config WHERE wallet_id=:walletId AND asset_id=:assetId")
    suspend fun getConfig(walletId: String, assetId: String): DbAssetConfig?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun linkAssetToWallet(link: DbAssetWallet)

    @Query("SELECT * FROM asset WHERE id = :id")
    fun getAsset(id: String): DbAsset?

    @Query("SELECT * FROM asset_links WHERE asset_id = :assetId")
    fun getAssetLinks(assetId: String): Flow<List<DbAssetLink>>

    @Query("SELECT * FROM asset_market WHERE asset_id = :assetId")
    fun getAssetMarket(assetId: String): Flow<DbAssetMarket?>
}