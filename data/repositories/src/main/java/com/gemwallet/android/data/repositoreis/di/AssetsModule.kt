package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.aptos.AptosBalanceClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBalanceClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmBalanceClient
import com.gemwallet.android.blockchain.clients.near.NearBalanceClient
import com.gemwallet.android.blockchain.clients.solana.SolanaBalanceClient
import com.gemwallet.android.blockchain.clients.sui.SuiBalanceClient
import com.gemwallet.android.blockchain.clients.ton.TonBalanceClient
import com.gemwallet.android.blockchain.clients.tron.TronBalanceClient
import com.gemwallet.android.blockchain.clients.xrp.XrpBalanceClient
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.tokens.GetTokensCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.assets.BalancesRemoteSource
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.PricesDao
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
object AssetsModule {
    @Provides
    @Singleton
    fun provideAssetsRepository(
        gemApiClient: GemApiClient,
        assetsDao: AssetsDao,
        balancesDao: BalancesDao,
        pricesDao: PricesDao,
        sessionRepository: SessionRepository,
        balancesRemoteSource: BalancesRemoteSource,
        getTransactionsCase: GetTransactionsCase,
        getTokensCase: GetTokensCase,
        searchTokensCase: SearchTokensCase,
        getDeviceIdCase: GetDeviceIdCase,
    ): AssetsRepository = AssetsRepository(
        gemApi = gemApiClient,
        assetsDao = assetsDao,
        balancesDao = balancesDao,
        pricesDao = pricesDao,
        sessionRepository = sessionRepository,
        getTransactionsCase = getTransactionsCase,
        balancesRemoteSource = balancesRemoteSource,
        getTokensCase = getTokensCase,
        searchTokensCase = searchTokensCase,
        getDeviceIdCase = getDeviceIdCase,
    )

    @Provides
    @Singleton
    fun provideBalanceRemoteSource(
        rpcClients: RpcClientAdapter,
    ): BalancesRemoteSource = BalancesRemoteSource(
        Chain.available().mapNotNull {
            when (it) {
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin -> BitcoinBalanceClient(it, rpcClients.getClient(it))

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
                Chain.Ethereum -> EvmBalanceClient(it, rpcClients.getClient(it))

                Chain.Solana -> SolanaBalanceClient(it, rpcClients.getClient(Chain.Solana))
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> CosmosBalanceClient(it, rpcClients.getClient(it))

                Chain.Ton -> TonBalanceClient(it, rpcClients.getClient(Chain.Ton))
                Chain.Tron -> TronBalanceClient(it, rpcClients.getClient(Chain.Tron))
                Chain.Aptos -> AptosBalanceClient(it, rpcClients.getClient(it))
                Chain.Sui -> SuiBalanceClient(it, rpcClients.getClient(it))
                Chain.Xrp -> XrpBalanceClient(it, rpcClients.getClient(it))
                Chain.Near -> NearBalanceClient(it, rpcClients.getClient(it))
            }
        }
    )
}

