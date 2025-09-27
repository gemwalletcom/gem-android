package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.cases.device.RequestPushToken
import com.gemwallet.android.flavors.StoreRequestPushToken
import com.gemwallet.android.flavors.isNotificationsAvailable
import com.gemwallet.android.model.BuildInfo
import com.gemwallet.android.model.NotificationsAvailable
import com.gemwallet.android.services.ShowSystemNotification
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
    fun provideBuildInfo(
        platformStore: PlatformStore,
        requestPushToken: RequestPushToken,
    ): BuildInfo {
        return BuildInfo(
            platformStore = platformStore,
            versionName = com.gemwallet.android.BuildConfig.VERSION_NAME,
            requestPushToken = requestPushToken,
        )
    }

    @Provides
    @Singleton
    fun provideRequestPushToken(): RequestPushToken {
        return StoreRequestPushToken()
    }

    @Provides
    @Singleton
    fun provideShowSystemNotification(@ApplicationContext context: Context): com.gemwallet.android.cases.pushes.ShowSystemNotification {
        return ShowSystemNotification(context)
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