package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
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
object StakeModule {
    @Singleton
    @Provides
    fun provideStakeRepository(
        stakeDao: StakeDao,
        gateway: GemGateway,
        rpcClients: RpcClientAdapter,
        gemApiStaticClient: GemApiStaticClient,
        gemApiClient: GemApiClient,
    ): StakeRepository = StakeRepository(
        stakeDao = stakeDao,
        gemApiStaticClient = gemApiStaticClient,
        gemApiClient = gemApiClient,
        gateway = gateway,
        stakeClients = Chain.available().mapNotNull {
            when (it.toChainType()) {
                ChainType.Ethereum -> when (it) {
                    Chain.SmartChain -> SmartchainStakeClient(it, rpcClients.getClient(it))
                    else -> null
                }
                else -> null
//                ChainType.Tron -> TronStakeClient(it, rpcClients.getClient(it), rpcClients.getClient(it))
            }
        }
    )
}

