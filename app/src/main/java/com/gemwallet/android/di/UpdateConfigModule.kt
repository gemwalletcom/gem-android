package com.gemwallet.android.di

import com.gemwallet.android.BuildConfig
import com.gemwallet.android.model.AppUpdateConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object UpdateConfigModule {
    @Provides
    fun provideAppUpdateConfig(): AppUpdateConfig {
        return AppUpdateConfig(BuildConfig.UPDATE_URL)
    }
}