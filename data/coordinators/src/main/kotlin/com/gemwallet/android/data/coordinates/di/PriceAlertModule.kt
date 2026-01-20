package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.pricealerts.coordinators.ExcludePriceAlert
import com.gemwallet.android.application.pricealerts.coordinators.GetAssetPriceAlertState
import com.gemwallet.android.application.pricealerts.coordinators.GetPriceAlerts
import com.gemwallet.android.application.pricealerts.coordinators.IncludePriceAlert
import com.gemwallet.android.application.pricealerts.coordinators.PriceAlertsStateCoordinator
import com.gemwallet.android.application.pricealerts.coordinators.SyncPriceAlerts
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.data.coordinates.pricealerts.ExcludePriceAlertImpl
import com.gemwallet.android.data.coordinates.pricealerts.GetAssetPriceAlertStateImpl
import com.gemwallet.android.data.coordinates.pricealerts.GetPriceAlertsImpl
import com.gemwallet.android.data.coordinates.pricealerts.IncludePriceAlertImpl
import com.gemwallet.android.data.coordinates.pricealerts.PriceAlertsStateCoordinatorImpl
import com.gemwallet.android.data.coordinates.pricealerts.SyncPriceAlertsImpl
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PriceAlertModule {
    @Provides
    @Singleton
    fun provideAddPriceAlerts(
        gemApiClient: GemApiClient,
        getDeviceId: GetDeviceId,
        priceAlertRepository: PriceAlertRepository,
        sessionRepository: SessionRepository,
    ): IncludePriceAlert {
        return IncludePriceAlertImpl(
            gemApiClient = gemApiClient,
            getDeviceIdImpl = getDeviceId,
            priceAlertRepository = priceAlertRepository,
            sessionRepository = sessionRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetPriceAlerts(
        priceAlertRepository: PriceAlertRepository,
        assetsRepository: AssetsRepository,
    ): GetPriceAlerts {
        return GetPriceAlertsImpl(
            priceAlertRepository = priceAlertRepository,
            assetsRepository = assetsRepository,
        )
    }

    @Provides
    fun providePriceAlertsStateCoordinator(
        getPushEnabled: GetPushEnabled,
        priceAlertRepository: PriceAlertRepository,
        includePriceAlert: IncludePriceAlert,
        excludePriceAlert: ExcludePriceAlert,
        syncDeviceInfo: SyncDeviceInfo,
        switchPushEnabled: SwitchPushEnabled,
        walletsRepository: WalletsRepository,
    ): PriceAlertsStateCoordinator {
        return PriceAlertsStateCoordinatorImpl(
            getPushEnabled = getPushEnabled,
            priceAlertRepository = priceAlertRepository,
            includePriceAlert = includePriceAlert,
            excludePriceAlert = excludePriceAlert,
            syncDeviceInfo = syncDeviceInfo,
            switchPushEnabled = switchPushEnabled,
            walletsRepository = walletsRepository,
        )
    }

    @Provides
    @Singleton
    fun providePriceAlertExclude(
        gemApiClient: GemApiClient,
        sessionRepository: SessionRepository,
        getDeviceId: GetDeviceId,
        priceAlertRepository: PriceAlertRepository,
    ): ExcludePriceAlert {
        return ExcludePriceAlertImpl(
            getDeviceId = getDeviceId,
            gemApiClient = gemApiClient,
            sessionRepository = sessionRepository,
            priceAlertRepository = priceAlertRepository,
        )
    }

    @Provides
    @Singleton
    fun provideAssetPriceAlertState(
        priceAlertRepository: PriceAlertRepository,
    ): GetAssetPriceAlertState {
        return GetAssetPriceAlertStateImpl(
            priceAlertRepository = priceAlertRepository,
        )
    }

    @Provides
    fun provideSyncPriceAlerts(
        gemApiClient: GemApiClient,
        getDeviceId: GetDeviceId,
        priceAlertRepository: PriceAlertRepository,
    ): SyncPriceAlerts {
        return SyncPriceAlertsImpl(
            getDeviceId = getDeviceId,
            gemApiClient = gemApiClient,
            priceAlertRepository = priceAlertRepository,
        )
    }
}