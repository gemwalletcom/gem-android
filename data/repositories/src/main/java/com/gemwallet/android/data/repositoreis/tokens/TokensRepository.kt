package com.gemwallet.android.data.repositoreis.tokens

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.cases.tokens.GetTokensCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.service.store.database.TokensDao
import com.gemwallet.android.data.service.store.database.entities.DbToken
import com.gemwallet.android.data.service.store.database.mappers.AssetInfoMapper
import com.gemwallet.android.data.service.store.database.mappers.TokenMapper
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetProperties
import com.wallet.core.primitives.AssetScore
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TokensRepository (
    private val tokensDao: TokensDao,
    private val gemApiClient: GemApiClient,
    private val getTokenClients: List<GetTokenClient>,
) : GetTokensCase, SearchTokensCase {
    private val mapper = TokenMapper()

    override suspend fun getByIds(ids: List<AssetId>): List<Asset> = withContext(Dispatchers.IO) {
        tokensDao.getById(ids.map { it.toIdentifier() }).map(mapper::asEntity)
    }

    override fun getByChains(chains: List<Chain>, query: String): Flow<List<Asset>> {
        return tokensDao.search(chains.mapNotNull { chain -> getTokenType(chain) }, query)
            .map { assets -> assets.map(mapper::asEntity) }
    }

    override suspend fun search(query: String): Boolean = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext false
        }
        val tokens = gemApiClient.search(query).getOrNull()
        val assets = if (tokens.isNullOrEmpty()) {
            val assets = getTokenClients.map {
                async {
                    try {
                        if (it.isTokenQuery(query)) {
                            it.getTokenData(query)
                        } else {
                            null
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
            .awaitAll()
            .mapNotNull { it }
            .map { AssetFull(asset = it, score = AssetScore(0), links = emptyList(), properties = AssetProperties(false, false, false, false)) }
            addTokens(assets)
            assets
        } else {
            val assets = tokens.filter { it.asset.id != null }
            addTokens(assets)
            assets
        }
        assets.isNotEmpty()
    }

    override suspend fun search(assetId: AssetId): Boolean {
        val tokenId = assetId.tokenId ?: return false
        val asset = getTokenClients
            .firstOrNull { it.supported(assetId.chain) && it.isTokenQuery(tokenId) }
            ?.getTokenData(tokenId)
        if (asset == null) {
            return search(tokenId)
        }
        addTokens(listOf(AssetFull(asset, score = AssetScore(0), links = emptyList(), properties = AssetProperties(false, false, false, false))))
        return true
    }

    override suspend fun assembleAssetInfo(assetId: AssetId): Flow<AssetInfo?> {
        return tokensDao.assembleAssetInfo(assetId.chain, assetId.toIdentifier())
            .map { AssetInfoMapper().asDomain(it).firstOrNull() }
    }

    private suspend fun addTokens(tokens: List<AssetFull>) {
        tokensDao.insert(tokens.map { token ->
            DbToken(
                id = token.asset.id.toIdentifier(),
                name = token.asset.name,
                symbol = token.asset.symbol,
                decimals = token.asset.decimals,
                type = token.asset.type,
                rank = token.score.rank,
            )
        })
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
        Chain.World,
        Chain.Ethereum -> AssetType.ERC20

        Chain.Solana -> AssetType.SPL
        Chain.Tron -> AssetType.TRC20

        Chain.Aptos,
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