package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.ethereum.EvmGetTokenClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTokenClient
import com.gemwallet.android.blockchain.clients.sui.SuiGetTokenClient
import com.gemwallet.android.blockchain.clients.ton.TonGetTokenClient
import com.gemwallet.android.blockchain.clients.tron.TronGetTokenClient
import com.gemwallet.android.cases.tokens.GetTokensCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.tokens.TokensRepository
import com.gemwallet.android.data.service.store.database.TokensDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.available
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TokensModule {
    @Provides
    @Singleton
    fun provideTokensRepository(
        tokensDao: TokensDao,
        gemApiClient: GemApiClient,
        rpcClients: RpcClientAdapter,
): TokensRepository = TokensRepository(
        tokensDao = tokensDao,
        gemApiClient = gemApiClient,
        getTokenClients = Chain.available().mapNotNull {
            when (it) {
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.World,
                Chain.Ethereum -> EvmGetTokenClient(it, rpcClients.getClient(it))

                Chain.Tron -> TronGetTokenClient(it, rpcClients.getClient(Chain.Tron))
                Chain.Solana -> SolanaTokenClient(it, rpcClients.getClient(Chain.Solana))
                Chain.Sui -> SuiGetTokenClient(it, rpcClients.getClient(it))
                Chain.Ton -> TonGetTokenClient(it, rpcClients.getClient(it))
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin,
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos,
                Chain.Aptos,
                Chain.Xrp,
                Chain.Near -> null
            }
        }
    )

    @Provides
    @Singleton
    fun provideGetTokensCase(tokensRepository: TokensRepository): GetTokensCase = tokensRepository

    @Provides
    @Singleton
    fun provideSearchTokensCase(tokensRepository: TokensRepository): SearchTokensCase = tokensRepository
}

