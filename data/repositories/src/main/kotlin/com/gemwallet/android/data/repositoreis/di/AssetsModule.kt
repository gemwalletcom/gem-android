package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.algorand.AlgorandBalanceClient
import com.gemwallet.android.blockchain.clients.aptos.AptosBalanceClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBalanceClient
import com.gemwallet.android.blockchain.clients.cardano.CardanoBalanceClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.blockchain.clients.near.NearBalanceClient
import com.gemwallet.android.blockchain.clients.polkadot.PolkadotBalancesClient
import com.gemwallet.android.blockchain.clients.solana.SolanaBalanceClient
import com.gemwallet.android.blockchain.clients.stellar.StellarBalanceClient
import com.gemwallet.android.blockchain.clients.sui.SuiBalanceClient
import com.gemwallet.android.blockchain.clients.ton.TonBalanceClient
import com.gemwallet.android.blockchain.clients.tron.TronBalanceClient
import com.gemwallet.android.blockchain.clients.xrp.XrpBalanceClient
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.assets.BalancesRemoteSource
import com.gemwallet.android.data.repositoreis.assets.PriceWebSocketClient
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.service.store.database.PricesDao
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
object AssetsModule {
    @Provides
    @Singleton
    fun provideAssetsRepository(
        gemApiClient: GemApiClient,
        assetsDao: AssetsDao,
        assetsPriorityDao: AssetsPriorityDao,
        balancesDao: BalancesDao,
        pricesDao: PricesDao,
        sessionRepository: SessionRepository,
        balancesRemoteSource: BalancesRemoteSource,
        getTransactions: GetTransactions,
        searchTokensCase: SearchTokensCase,
        getDeviceIdCase: GetDeviceIdCase,
        priceClient: PriceWebSocketClient,
    ): AssetsRepository = AssetsRepository(
        gemApi = gemApiClient,
        assetsDao = assetsDao,
        assetsPriorityDao = assetsPriorityDao,
        balancesDao = balancesDao,
        pricesDao = pricesDao,
        sessionRepository = sessionRepository,
        getTransactions = getTransactions,
        balancesRemoteSource = balancesRemoteSource,
        searchTokensCase = searchTokensCase,
        getDeviceIdCase = getDeviceIdCase,
        priceClient = priceClient
    )

    @Provides
    @Singleton
    fun provideBalanceRemoteSource(
        rpcClients: RpcClientAdapter,
    ): BalancesRemoteSource = BalancesRemoteSource(
        Chain.available().map {
            when (it.toChainType()) {
                ChainType.Bitcoin -> BitcoinBalanceClient(it, rpcClients.getClient(it))
                ChainType.Ethereum -> EvmBalanceClient(it, rpcClients.getClient(it), rpcClients.getClient(it), SmartchainStakeClient(it, rpcClients.getClient(it)))
                ChainType.Solana -> SolanaBalanceClient(it, rpcClients.getClient(Chain.Solana), rpcClients.getClient(Chain.Solana), rpcClients.getClient(Chain.Solana))
                ChainType.Cosmos -> CosmosBalanceClient(it, rpcClients.getClient(it), rpcClients.getClient(it))
                ChainType.Ton -> TonBalanceClient(it, rpcClients.getClient(Chain.Ton))
                ChainType.Tron -> TronBalanceClient(it, rpcClients.getClient(Chain.Tron), rpcClients.getClient(Chain.Tron), rpcClients.getClient(Chain.Tron))
                ChainType.Aptos -> AptosBalanceClient(it, rpcClients.getClient(it))
                ChainType.Sui -> SuiBalanceClient(it, rpcClients.getClient(it))
                ChainType.Xrp -> XrpBalanceClient(it, rpcClients.getClient(it))
                ChainType.Near -> NearBalanceClient(it, rpcClients.getClient(it))
                ChainType.Algorand -> AlgorandBalanceClient(it, rpcClients.getClient(it))
                ChainType.Stellar -> StellarBalanceClient(it, rpcClients.getClient(it))
                ChainType.Polkadot -> PolkadotBalancesClient(it, rpcClients.getClient(it))
                ChainType.Cardano -> CardanoBalanceClient(it, rpcClients.getClient(it))
            }
        }
    )

    @Provides
    @Singleton
    fun providePriceClient(
        sessionRepository: SessionRepository,
        assetsDao: AssetsDao,
        pricesDao: PricesDao,
        priceAlertsDao: PriceAlertsDao,
    ): PriceWebSocketClient {
        return PriceWebSocketClient(
            sessionRepository = sessionRepository,
            assetsDao = assetsDao,
            pricesDao = pricesDao,
            priceAlertsDao = priceAlertsDao
        )
    }
}