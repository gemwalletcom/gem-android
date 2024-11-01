package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbAssetInfo
import com.gemwallet.android.data.service.store.database.entities.DbToken
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

@Dao
interface TokensDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(token: DbToken)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(token: List<DbToken>)

    @Query("DELETE FROM tokens WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("SELECT * FROM tokens WHERE id IN (:ids)")
    suspend fun getById(ids: List<String>): List<DbToken>

    @Query("SELECT * FROM tokens WHERE type IN (:types) ORDER BY rank DESC")
    suspend fun getByType(types: List<AssetType>): List<DbToken>

    @Query("SELECT * FROM tokens WHERE type IN (:types) " +
            "AND (id LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%') " +
            "COLLATE NOCASE" +
            " ORDER BY rank DESC"
    )
    fun search(types: List<AssetType>, query: String): Flow<List<DbToken>>

    @Query("""
        SELECT
            tokens.id as id,
            tokens.name as name,
            tokens.symbol as symbol,
            tokens.decimals as decimals,
            tokens.type as type,
            accounts.derivation_path as derivationPath,
            accounts.chain as chain,
            accounts.wallet_id as walletId,
            accounts.address AS address,
            accounts.extendedPublicKey AS extendedPublicKey,
            wallets.type AS walletType,
            wallets.name AS walletName,
            0 AS pinned,
            0 AS visible,
            0 AS position,
            0 AS isBuyEnabled,
            0 AS isSwapEnabled,
            0 AS isStakeEnabled,
            0 AS assetRank
        FROM tokens, accounts
        JOIN wallets ON wallets.id = accounts.wallet_id
        WHERE
            accounts.wallet_id = (SELECT wallet_id FROM session WHERE session.id = 1)
            AND accounts.chain = :chain
            AND tokens.id = :assetId
        """)
    suspend fun assembleAssetInfo(chain: Chain, assetId: String): List<DbAssetInfo>
}