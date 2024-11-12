package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushEnabledCase
import com.gemwallet.android.cases.device.GetPushTokenCase
import com.gemwallet.android.cases.device.SetPushTokenCase
import com.gemwallet.android.cases.device.SwitchPushEnabledCase
import com.gemwallet.android.cases.device.SyncDeviceInfoCase
import com.gemwallet.android.cases.device.SyncSubscriptionCase
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.data.repositoreis.device.DeviceRepository
import com.gemwallet.android.data.repositoreis.device.GetDeviceId
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.services.gemapi.GemApiClient
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
    fun provideDeviceIdCase(@ApplicationContext context: Context): GetDeviceIdCase
        = GetDeviceId(ConfigStore(context.getSharedPreferences("device-info", Context.MODE_PRIVATE)))

    @Provides
    @Singleton
    fun provideDeviceRepository(
        @ApplicationContext context: Context,
        buildInfo: BuildInfo,
        gemApiClient: GemApiClient,
        getDeviceIdCase: GetDeviceIdCase,
        enablePriceAlertCase: EnablePriceAlertCase,
        getCurrentCurrencyCase: GetCurrentCurrencyCase,
    ): DeviceRepository {
        return DeviceRepository(
            gemApiClient = gemApiClient,
            configStore = ConfigStore(context.getSharedPreferences("device-info", Context.MODE_PRIVATE)),
            requestPushToken = buildInfo.requestPushToken,
            platformStore = buildInfo.platformStore,
            versionName = buildInfo.versionName,
            getDeviceIdCase = getDeviceIdCase,
            enablePriceAlertCase = enablePriceAlertCase,
            getCurrentCurrencyCase = getCurrentCurrencyCase,
        )
    }

    @Provides
    fun provideSyncDeviceInfoCase(repository: DeviceRepository): SyncDeviceInfoCase = repository

    @Provides
    fun provideSwitchPushEnabledCase(repository: DeviceRepository): SwitchPushEnabledCase = repository

    @Provides
    fun provideGetPushEnabledCase(repository: DeviceRepository): GetPushEnabledCase = repository

    @Provides
    fun provideSetPushTokenCase(repository: DeviceRepository): SetPushTokenCase = repository

    @Provides
    fun provideGetPushTokenCase(repository: DeviceRepository): GetPushTokenCase = repository

    @Provides
    fun provideSyncSubscriptionCase(repository: DeviceRepository): SyncSubscriptionCase = repository
}