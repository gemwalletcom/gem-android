package com.gemwallet.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbAssetInfo
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM assets_info WHERE chain = :chain AND id = :assetId")
    fun getAssetInfo(assetId: String, chain: Chain): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM assets_info")
    fun getAssetsInfo(): Flow<List<DbAssetInfo>>

    @Query("SELECT * FROM assets_info WHERE id IN (:ids)")
    fun getAssetsInfo(ids: List<String>): Flow<List<DbAssetInfo>>

    @Query("""
        SELECT * FROM assets_info WHERE
            id LIKE '%' || :query || '%'
            OR symbol LIKE '%' || :query || '%'
            OR name LIKE '%' || :query || '%' COLLATE NOCASE
        """)
    fun searchAssetInfo(query: String): Flow<List<DbAssetInfo>>

    // TODO: Move to Database
    @Query("""
        SELECT
            assets.*,
            accounts.*,
            (SELECT currency FROM session WHERE id = 1) AS priceCurrency,
            wallets.type AS walletType,
            wallets.name AS walletName,
            prices.value AS priceValue,
            prices.dayChanged AS priceDayChanges,
            balances.amount AS amount,
            balances.type as balanceType
        FROM assets
        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
        JOIN wallets ON wallets.id = accounts.wallet_id
        LEFT JOIN balances ON assets.owner_address = balances.address AND assets.id = balances.asset_id
        LEFT JOIN prices ON assets.id = prices.assetId
        WHERE accounts.address in (:accounts)
    """)
    fun getAssetsInfoByAccounts(accounts: List<String>): Flow<List<DbAssetInfo>>
}