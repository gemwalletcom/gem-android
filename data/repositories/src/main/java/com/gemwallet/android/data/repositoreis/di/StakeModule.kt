package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.cosmos.CosmosStakeClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.blockchain.clients.solana.SolanaStakeClient
import com.gemwallet.android.blockchain.clients.sui.SuiStakeClient
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.services.gemapi.http.GemApiStaticClient
import com.gemwallet.android.ext.available
import com.wallet.core.primitives.Chain
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
            when (it) {
                Chain.Bitcoin,
                Chain.Litecoin,
                Chain.Ethereum,
                Chain.Blast,
                Chain.Polygon,
                Chain.Arbitrum,
                Chain.Ton,
                Chain.Tron,
                Chain.Doge,
                Chain.Optimism,
                Chain.Aptos,
                Chain.Base,
                Chain.AvalancheC,
                Chain.Xrp,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Manta,
                Chain.Thorchain,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Near,
                Chain.World,
                Chain.Gnosis -> null

                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> CosmosStakeClient(it, rpcClients.getClient(it))

                Chain.Sui -> SuiStakeClient(rpcClients.getClient(it))
                Chain.Solana -> SolanaStakeClient(rpcClients.getClient(it))
                Chain.SmartChain -> SmartchainStakeClient(rpcClients.getClient(it))
            }
        }
    )
}

