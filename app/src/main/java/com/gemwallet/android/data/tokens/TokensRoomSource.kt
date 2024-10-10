package com.gemwallet.android.data.tokens

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.database.entities.DbAssetInfo
import com.gemwallet.android.data.database.mappers.AssetInfoMapper
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


@Entity(tableName = "tokens", primaryKeys = ["id"])
data class TokenRoom(
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val rank: Int,
)

@Dao
interface TokensDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: TokenRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: List<TokenRoom>)

    @Query("DELETE FROM tokens WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("SELECT * FROM tokens WHERE id IN (:ids)")
    suspend fun getById(ids: List<String>): List<TokenRoom>

    @Query("SELECT * FROM tokens WHERE type IN (:types) ORDER BY rank DESC")
    suspend fun getByType(types: List<AssetType>): List<TokenRoom>

    @Query("SELECT * FROM tokens WHERE type IN (:types) " +
            "AND (id LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%') " +
            "COLLATE NOCASE" +
            " ORDER BY rank DESC"
    )
    fun search(types: List<AssetType>, query: String): Flow<List<TokenRoom>>

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
    fun assembleAssetInfo(chain: Chain, assetId: String): List<DbAssetInfo>
}


class TokensRoomSource(
    private val tokensDao: TokensDao,
) : TokensLocalSource {



    override suspend fun assembleAssetInfo(assetId: AssetId): AssetInfo? = withContext(Dispatchers.IO) {
        val dbAssetInfo = tokensDao.assembleAssetInfo(assetId.chain, assetId.toIdentifier())
        AssetInfoMapper().asDomain(dbAssetInfo).firstOrNull()
    }
}