package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.cases.device.GetDeviceIdOld
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.data.repositoreis.device.DeviceRepository
import com.gemwallet.android.data.repositoreis.device.GetDeviceIdOldImpl
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.model.BuildInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DeviceModule {

    @Provides
    @Singleton
    fun provideDeviceOldId(@ApplicationContext context: Context): GetDeviceIdOld
        = GetDeviceIdOldImpl(ConfigStore(context.getSharedPreferences("device-info", Context.MODE_PRIVATE)))

    @Provides
    @Singleton
    fun provideDeviceRepository(
        @ApplicationContext context: Context,
        buildInfo: BuildInfo,
        gemApiClient: GemApiClient,
        gemDeviceApiClient: GemDeviceApiClient,
        getDeviceIdOld: GetDeviceIdOld,
        getDeviceId: GetDeviceId,
        priceAlertRepository: PriceAlertRepository,
        getCurrentCurrencyCase: GetCurrentCurrencyCase,
    ): DeviceRepository {
        return DeviceRepository(
            context = context,
            gemApiClient = gemApiClient,
            gemDeviceApiClient = gemDeviceApiClient,
            getDeviceIdOld = getDeviceIdOld,
            getDeviceId = getDeviceId,
            configStore = ConfigStore(context.getSharedPreferences("device-info", Context.MODE_PRIVATE)),
            requestPushToken = buildInfo.requestPushToken,
            platformStore = buildInfo.platformStore,
            versionName = buildInfo.versionName,
            priceAlertRepository = priceAlertRepository,
            getCurrentCurrencyCase = getCurrentCurrencyCase,
        )
    }

    @Provides
    fun provideSyncDeviceInfoCase(repository: DeviceRepository): SyncDeviceInfo = repository

    @Provides
    fun provideSwitchPushEnabledCase(repository: DeviceRepository): SwitchPushEnabled = repository

    @Provides
    fun provideGetPushEnabledCase(repository: DeviceRepository): GetPushEnabled = repository

    @Provides
    fun provideSetPushTokenCase(repository: DeviceRepository): SetPushToken = repository

    @Provides
    fun provideGetPushTokenCase(repository: DeviceRepository): GetPushToken = repository

    @Provides
    fun provideSyncSubscriptionCase(repository: DeviceRepository): SyncSubscription = repository
}