package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.cases.pushes.ShowSystemNotification
import com.gemwallet.android.flavors.StoreRequestPushToken
import com.gemwallet.android.flavors.isNotificationsAvailable
import com.gemwallet.android.model.BuildInfo
import com.gemwallet.android.model.NotificationsAvailable
import com.wallet.core.primitives.PlatformStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object BuildInfoModule {

    @Provides
    @Singleton
    fun provideBuildInfo(platformStore: PlatformStore): BuildInfo {
        return BuildInfo(
            platformStore = platformStore,
            versionName = com.gemwallet.android.BuildConfig.VERSION_NAME,
            requestPushToken = StoreRequestPushToken(),
        )
    }

    @Provides
    @Singleton
    fun provideShowSystemNotification(@ApplicationContext context: Context): ShowSystemNotification {
        return com.gemwallet.android.features.notifications.ShowSystemNotification(context)
    }

    @Provides
    @Singleton
    fun provideNotificationEnabled(@ApplicationContext context: Context): NotificationsAvailable {
        return isNotificationsAvailable()
    }

    @Provides
    @Singleton
    fun providePlatformStore(): PlatformStore {
        return when (com.gemwallet.android.BuildConfig.FLAVOR) {
            "google" -> PlatformStore.GooglePlay
            "universal" -> PlatformStore.ApkUniversal
            "huawei" -> PlatformStore.Huawei
            "solana" -> PlatformStore.SolanaStore
            "fdroid" -> PlatformStore.Fdroid
            "samsung" -> PlatformStore.SamsungStore
            else -> PlatformStore.Local
        }
    }
}