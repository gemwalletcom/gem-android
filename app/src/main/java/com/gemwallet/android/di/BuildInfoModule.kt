package com.gemwallet.android.di

import com.gemwallet.android.model.BuildInfo
import com.gemwallet.android.services.StoreRequestPushToken
import com.wallet.core.primitives.PlatformStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object BuildInfoModule {

    @Provides
    @Singleton
    fun provideBuildInfo(): BuildInfo {
        val platformStore = when (com.gemwallet.android.BuildConfig.FLAVOR) {
            "google" -> PlatformStore.GooglePlay
            "universal" -> PlatformStore.ApkUniversal
            "huawei" -> PlatformStore.Huawei
            "solana" -> PlatformStore.SolanaStore
            "fdroid" -> PlatformStore.Fdroid
            else -> PlatformStore.Local
        }
        return BuildInfo(
            platformStore = platformStore,
            versionName = com.gemwallet.android.BuildConfig.VERSION_NAME,
            requestPushToken = StoreRequestPushToken(),
        )
    }
}