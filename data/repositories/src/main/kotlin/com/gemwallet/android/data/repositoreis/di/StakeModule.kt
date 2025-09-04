package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.services.StakeService
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemGateway
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object StakeModule {
    @Singleton
    @Provides
    fun provideStakeRepository(
        stakeDao: StakeDao,
        gateway: GemGateway,
        gemApiStaticClient: GemApiStaticClient,
        gemApiClient: GemApiClient,
    ): StakeRepository = StakeRepository(
        stakeDao = stakeDao,
        gemApiStaticClient = gemApiStaticClient,
        gemApiClient = gemApiClient,
        stakeService = StakeService(gateway),
    )
}

