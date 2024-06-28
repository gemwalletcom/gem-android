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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


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
    fun insert(token: TokenRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(token: List<TokenRoom>)

    @Query("DELETE FROM tokens WHERE id IN (:ids)")
    fun delete(ids: List<String>)

    @Query("SELECT * FROM tokens WHERE id IN (:ids)")
    fun getById(ids: List<String>): List<TokenRoom>

    @Query("SELECT * FROM tokens WHERE type IN (:types) ORDER BY rank DESC")
    fun getByType(types: List<AssetType>): List<TokenRoom>

    @Query("SELECT * FROM tokens WHERE type IN (:types) " +
            "AND (id LIKE '%' || :query || '%' OR symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%') " +
            "COLLATE NOCASE" +
            " ORDER BY rank DESC"
    )
    fun search(types: List<AssetType>, query: String): Flow<List<TokenRoom>>

    @Query("""
        SELECT
            tokens.*,
            accounts.*,
            accounts.address AS owner_address,
            wallets.type AS walletType,
            wallets.name AS walletName,
            0 AS is_visible,
            0 AS is_buy_enabled,
            0 AS is_swap_enabled,
            0 AS is_stake_enabled
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

    override suspend fun addTokens(tokens: List<AssetFull>) {
        tokensDao.insert(tokens.map { token ->
            TokenRoom(
                id = token.asset.id.toIdentifier(),
                name = token.asset.name,
                symbol = token.asset.symbol,
                decimals = token.asset.decimals,
                type = token.asset.type,
                rank = token.score.rank,
            )
        })
    }

    override suspend fun getByIds(ids: List<AssetId>): List<Asset> {
        return tokensDao.getById(ids.map { it.toIdentifier() })
            .mapNotNull {
                Asset(
                    id = it.id.toAssetId() ?: return@mapNotNull null,
                    name = it.name,
                    symbol = it.symbol,
                    decimals = it.decimals,
                    type = it.type,
                )
            }
    }

    override suspend fun getByChains(chains: List<Chain>): List<Asset> {
        return tokensDao.getByType(
            chains.mapNotNull {  chain -> getTokenType(chain) }
        ).mapNotNull {
            Asset(
                id = it.id.toAssetId() ?: return@mapNotNull null,
                name = it.name,
                symbol = it.symbol,
                decimals = it.decimals,
                type = it.type,
            )
        }
    }

    override suspend fun search(chains: List<Chain>, query: String): Flow<List<Asset>> {
        return tokensDao.search(chains.mapNotNull {  chain -> getTokenType(chain) }, query).map { assets ->
            assets.mapNotNull {
                Asset(
                    id = it.id.toAssetId() ?: return@mapNotNull null,
                    name = it.name,
                    symbol = it.symbol,
                    decimals = it.decimals,
                    type = it.type,
                )
            }
        }
    }

    override suspend fun assembleAssetInfo(assetId: AssetId): AssetInfo? {
        val dbAssetInfo = tokensDao.assembleAssetInfo(assetId.chain, assetId.toIdentifier())
        return AssetInfoMapper().asDomain(dbAssetInfo).firstOrNull()
    }

    private fun getTokenType(chain: Chain) = when (chain) {
        Chain.SmartChain -> AssetType.BEP20
        Chain.Base,
        Chain.AvalancheC,
        Chain.Polygon,
        Chain.Arbitrum,
        Chain.OpBNB,
        Chain.Manta,
        Chain.Fantom,
        Chain.Gnosis,
        Chain.Optimism,
        Chain.Blast,
        Chain.ZkSync,
        Chain.Linea,
        Chain.Mantle,
        Chain.Celo,
        Chain.Ethereum -> AssetType.ERC20
        Chain.Solana -> AssetType.SPL
        Chain.Tron -> AssetType.TRC20
        Chain.Sui -> AssetType.TOKEN
        Chain.Ton -> AssetType.JETTON
        Chain.Cosmos,
        Chain.Osmosis,
        Chain.Celestia,
        Chain.Thorchain,
        Chain.Injective,
        Chain.Noble,
        Chain.Sei -> AssetType.IBC
        Chain.Bitcoin,
        Chain.Litecoin,
        Chain.Doge,
        Chain.Aptos,
        Chain.Near,
        Chain.Xrp -> null
    }
}