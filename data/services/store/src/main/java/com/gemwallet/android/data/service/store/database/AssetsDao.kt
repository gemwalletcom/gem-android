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
import com.wallet.core.primitives.AssetType
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(asset: List<DbAsset>, links: List<DbAssetLink>)

    @Update
    fun update(asset: DbAsset)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(links: List<DbAssetLink>, market: DbAssetMarket)


    @Query("SELECT * FROM asset")
    suspend fun getAll(): List<DbAsset>

    @Query("SELECT * FROM asset_info WHERE chain = :chain AND id = :assetId AND sessionId = 1")
    fun getAssetInfo(assetId: String, chain: Chain): Flow<DbAssetInfo?>

    @Query("SELECT DISTINCT * FROM asset_info WHERE sessionId = 1 AND visible != 0 ORDER BY balanceFiatTotalAmount DESC")
    fun getAssetsInfo(): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE id IN (:ids) AND sessionId=1 ORDER BY balanceFiatTotalAmount DESC")
    fun getAssetsInfo(ids: List<String>): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE id IN (:ids) ORDER BY balanceFiatTotalAmount DESC")
    fun getAssetsInfoByAllWallets(ids: List<String>): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT * FROM asset_info WHERE
            id NOT IN (:exclude)
            AND (chain IN (SELECT chain FROM accounts JOIN session ON accounts.wallet_id = session.wallet_id AND session.id = 1))
            AND (walletId = (SELECT wallet_id FROM session WHERE session.id = 1) OR walletId IS NULL)
            AND (id LIKE '%' || :query || '%'
            OR symbol LIKE '%' || :query || '%'
            OR name LIKE '%' || :query || '%' COLLATE NOCASE)
            ORDER BY balanceFiatTotalAmount DESC
        """)
    fun searchAssetInfo(query: String, exclude: List<String> = emptyList()): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT * FROM asset_info WHERE
            id NOT IN (:exclude)
            AND (id LIKE '%' || :query || '%'
            OR symbol LIKE '%' || :query || '%'
            OR name LIKE '%' || :query || '%' COLLATE NOCASE)
            ORDER BY balanceFiatTotalAmount DESC
        """)
    fun searchAssetInfoByAllWallets(query: String, exclude: List<String> = emptyList()): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT * FROM asset_info WHERE
            (chain IN (SELECT chain FROM accounts JOIN session ON accounts.wallet_id = session.wallet_id AND session.id = 1))
            AND id NOT IN (:exclude)
            AND (chain IN (:byChains) OR id IN (:byAssets) )
            AND (id LIKE '%' || :query || '%'
            OR symbol LIKE '%' || :query || '%'
            OR name LIKE '%' || :query || '%' COLLATE NOCASE)
            ORDER BY balanceFiatTotalAmount DESC
        """)
    fun searchAssetInfo(query: String, exclude: List<String> = emptyList(), byChains: List<Chain>, byAssets: List<String>): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE address IN (:accounts) AND sessionId=1 ")
    fun getAssetsInfoByAccounts(accounts: List<String>): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE address IN (:accounts) AND walletId=:walletId ")
    fun getAssetsInfoByAccountsInWallet(accounts: List<String>, walletId: String): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_info WHERE address IN (:accounts) AND walletId=:walletId AND type=:type")
    fun getAssetsInfoByAccountsInWallet(accounts: List<String>, walletId: String, type: AssetType): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM asset_config WHERE wallet_id=:walletId AND asset_id=:assetId")
    suspend fun getConfig(walletId: String, assetId: String): DbAssetConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: DbAssetConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: List<DbAssetConfig>)

    @Query("UPDATE asset SET is_swap_enabled=1 WHERE chain IN (:chains)")
    suspend fun setSwapable(chains: List<Chain>)

    @Query("UPDATE asset SET is_swap_enabled=0")
    suspend fun resetSwapable()

    @Query("UPDATE asset SET is_buy_enabled=0")
    suspend fun resetBuyAvailable()

    @Query("UPDATE asset SET is_buy_enabled=1 WHERE id IN (:ids)")
    suspend fun updateBuyAvailable(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun linkAssetToWallet(link: DbAssetWallet)

    @Query("SELECT * FROM asset WHERE id = :id")
    fun getAsset(id: String): DbAsset?

    @Query("SELECT * FROM asset_links WHERE asset_id = :assetId")
    fun getAssetLinks(assetId: String): Flow<List<DbAssetLink>>

    @Query("SELECT * FROM asset_market WHERE asset_id = :assetId")
    fun getAssetMarket(assetId: String): Flow<DbAssetMarket?>
}