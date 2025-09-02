package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.ethereum.EvmBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.blockchain.services.BalancesService
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
import uniffi.gemstone.AlienProvider
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemPreferences
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
        balancesService: BalancesService,
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
        balancesService = balancesService,
        searchTokensCase = searchTokensCase,
        getDeviceIdCase = getDeviceIdCase,
        priceClient = priceClient
    )

    @Provides
    @Singleton
    fun provideGateway(alienProvider: AlienProvider): GemGateway {
        return GemGateway(
            alienProvider,
            object : GemPreferences {
                override fun get(key: String): String? {
                    TODO("Not yet implemented")
                }

                override fun set(key: String, value: String) {
                    TODO("Not yet implemented")
                }

                override fun remove(key: String) {
                    TODO("Not yet implemented")
                }
            },
            object : GemPreferences {
                override fun get(key: String): String? {
                    TODO("Not yet implemented")
                }

                override fun set(key: String, value: String) {
                    TODO("Not yet implemented")
                }

                override fun remove(key: String) {
                    TODO("Not yet implemented")
                }
            },
        )
    }

    @Provides
    @Singleton
    fun provideBalanceRemoteSource(
        rpcClients: RpcClientAdapter,
        gateway: GemGateway,
    ): BalancesService = BalancesService(
        gateway = gateway,
        balanceClients = Chain.available().mapNotNull {
            when (it.toChainType()) {
                ChainType.Ethereum -> EvmBalanceClient(it, rpcClients.getClient(it), rpcClients.getClient(it), SmartchainStakeClient(it, rpcClients.getClient(it)))
                else -> null
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