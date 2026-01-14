package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.application.transactions.coordinators.GetChangedTransactions
import com.gemwallet.android.blockchain.services.AddressStatusService
import com.gemwallet.android.blockchain.services.BalancesService
import com.gemwallet.android.blockchain.services.PerpetualService
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.assets.PriceWebSocketClient
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.service.store.database.PricesDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemGateway
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
        getChangedTransactions: GetChangedTransactions,
        searchTokensCase: SearchTokensCase,
        getDeviceId: GetDeviceId,
        priceClient: PriceWebSocketClient,
    ): AssetsRepository = AssetsRepository(
        gemApi = gemApiClient,
        assetsDao = assetsDao,
        assetsPriorityDao = assetsPriorityDao,
        balancesDao = balancesDao,
        pricesDao = pricesDao,
        sessionRepository = sessionRepository,
        getChangedTransactions = getChangedTransactions,
        balancesService = balancesService,
        searchTokensCase = searchTokensCase,
        getDeviceId = getDeviceId,
        priceClient = priceClient
    )

    @Provides
    @Singleton
    fun provideBalanceRemoteSource(
        gateway: GemGateway,
    ): BalancesService = BalancesService(
        gateway = gateway,
    )

    @Provides
    @Singleton
    fun provideAddressStatusService(
        gateway: GemGateway,
    ): AddressStatusService = AddressStatusService(
        gateway = gateway,
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

    @Provides
    @Singleton
    fun providePerpetualRemoteSource(
        gateway: GemGateway,
    ): PerpetualService = PerpetualService(
        gateway = gateway,
    )
}