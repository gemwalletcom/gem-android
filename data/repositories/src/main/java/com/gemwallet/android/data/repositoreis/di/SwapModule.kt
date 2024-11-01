package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.ethereum.EvmSwapClient
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
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
object SwapModule {
    @Singleton
    @Provides
    fun provideSwapRepository(
        gemApiClient: GemApiClient,
        rpcClients: RpcClientAdapter,
    ): SwapRepository = SwapRepository(
        gemApiClient = gemApiClient,
        swapClients = Chain.available().mapNotNull {
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
                Chain.Ethereum -> EvmSwapClient(it, rpcClients.getClient(it))

                else -> null
            }
        }
    )

}

