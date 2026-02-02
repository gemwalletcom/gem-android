package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.SecurityStore
import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.data.coordinates.device.GetDeviceIdImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DeviceModule {
    @Provides
    @Singleton
    fun provideDeviceId(securityStore: SecurityStore<Any>): GetDeviceId = GetDeviceIdImpl(securityStore)
}