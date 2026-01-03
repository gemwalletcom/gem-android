package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.data.coordinates.GetAuthPayloadImpl
import com.gemwallet.android.data.services.gemapi.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideGetAuthPayload(
        gemApiClient: GemApiClient,
        getDeviceIdCase: GetDeviceIdCase,
        passwordStore: PasswordStore,
        loadPrivateKeyOperator: LoadPrivateKeyOperator,
    ): GetAuthPayload {
        return GetAuthPayloadImpl(
            gemApiClient = gemApiClient,
            getDeviceIdCase = getDeviceIdCase,
            passwordStore = passwordStore,
            loadPrivateKeyOperator = loadPrivateKeyOperator,
        )
    }
}