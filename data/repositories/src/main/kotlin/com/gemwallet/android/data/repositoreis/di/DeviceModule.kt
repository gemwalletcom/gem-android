package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
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
        enablePriceAlert: EnablePriceAlert,
        getCurrentCurrencyCase: GetCurrentCurrencyCase,
    ): DeviceRepository {
        return DeviceRepository(
            context = context,
            gemApiClient = gemApiClient,
            configStore = ConfigStore(context.getSharedPreferences("device-info", Context.MODE_PRIVATE)),
            requestPushToken = buildInfo.requestPushToken,
            platformStore = buildInfo.platformStore,
            versionName = buildInfo.versionName,
            getDeviceIdCase = getDeviceIdCase,
            enablePriceAlert = enablePriceAlert,
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