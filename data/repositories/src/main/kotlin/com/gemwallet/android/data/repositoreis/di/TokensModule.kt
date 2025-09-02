package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.ethereum.EvmGetTokenClient
import com.gemwallet.android.blockchain.services.TokenService
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.tokens.TokensRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.available
import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemGateway
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TokensModule {
    @Provides
    @Singleton
    fun provideTokensRepository(
        assetsDao: AssetsDao,
        assetsPriorityDao: AssetsPriorityDao,
        gateway: GemGateway,
        gemApiClient: GemApiClient,
        rpcClients: RpcClientAdapter,
): TokensRepository = TokensRepository(
        assetsDao = assetsDao,
        assetsPriorityDao = assetsPriorityDao,
        gemApiClient = gemApiClient,
        tokenService = TokenService(
            gateway = gateway,
            getTokenClients = Chain.available().mapNotNull { chain ->
                when (chain.toChainType()) {
                    ChainType.Ethereum -> EvmGetTokenClient(chain, rpcClients.getClient(chain))
                    else -> null
                }
            }
        ),

    )

    @Provides
    @Singleton
    fun provideSearchTokensCase(tokensRepository: TokensRepository): SearchTokensCase = tokensRepository
}

