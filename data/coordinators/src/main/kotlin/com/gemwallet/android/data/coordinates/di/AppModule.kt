package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.cases.device.GetDeviceId
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
        getDeviceId: GetDeviceId,
        passwordStore: PasswordStore,
        loadPrivateKeyOperator: LoadPrivateKeyOperator,
    ): GetAuthPayload {
        return GetAuthPayloadImpl(
            gemApiClient = gemApiClient,
            getDeviceId = getDeviceId,
            passwordStore = passwordStore,
            loadPrivateKeyOperator = loadPrivateKeyOperator,
        )
    }
}