package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.cosmos.CosmosStakeClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.blockchain.clients.solana.SolanaStakeClient
import com.gemwallet.android.blockchain.clients.sui.SuiStakeClient
import com.gemwallet.android.blockchain.clients.tron.TronStakeClient
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
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
object StakeModule {
    @Singleton
    @Provides
    fun provideStakeRepository(
        stakeDao: StakeDao,
        rpcClients: RpcClientAdapter,
        gemApiStaticClient: GemApiStaticClient,
    ): StakeRepository = StakeRepository(
        stakeDao = stakeDao,
        gemApiStaticClient = gemApiStaticClient,
        stakeClients = Chain.available().mapNotNull {
            when (it.toChainType()) {
                ChainType.Ethereum -> when (it) {
                    Chain.SmartChain -> SmartchainStakeClient(it, rpcClients.getClient(it))
                    else -> null
                }
                ChainType.Solana -> SolanaStakeClient(it, rpcClients.getClient(it))
                ChainType.Cosmos -> CosmosStakeClient(it, rpcClients.getClient(it))
                ChainType.Sui -> SuiStakeClient(it, rpcClients.getClient(it))
                ChainType.Tron -> TronStakeClient(it, rpcClients.getClient(it), rpcClients.getClient(it))
                ChainType.Bitcoin,
                ChainType.Ton,
                ChainType.Tron,
                ChainType.Aptos,
                ChainType.Xrp,
                ChainType.Algorand,
                ChainType.Stellar,
                ChainType.Polkadot,
                ChainType.Cardano,
                ChainType.Near -> null
            }
        }
    )
}

