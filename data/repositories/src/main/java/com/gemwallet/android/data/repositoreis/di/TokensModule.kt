package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.aptos.AptosGetTokenClient
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
import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
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
        getTokenClients = Chain.available().mapNotNull { chain ->
            when (chain.toChainType()) {
                ChainType.Ethereum -> EvmGetTokenClient(chain, rpcClients.getClient(chain))
                ChainType.Solana -> SolanaTokenClient(chain, rpcClients.getClient(chain))
                ChainType.Ton -> TonGetTokenClient(chain, rpcClients.getClient(chain))
                ChainType.Tron -> TronGetTokenClient(chain, rpcClients.getClient(chain))
                ChainType.Sui -> SuiGetTokenClient(chain, rpcClients.getClient(chain))
                ChainType.Aptos -> AptosGetTokenClient(chain, rpcClients.getClient(chain))
                ChainType.Bitcoin,
                ChainType.Cosmos,
                ChainType.Xrp,
                ChainType.Algorand,
                ChainType.Stellar,
                ChainType.Polkadot,
                ChainType.Cardano,
                ChainType.Near -> null
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

