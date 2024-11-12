package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepositoryImpl
import com.gemwallet.android.data.service.store.database.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SessionModule {
    @Singleton
    @Provides
    fun provideSessionRepository(
        sessionDao: SessionDao,
        walletsRepository: com.gemwallet.android.data.repositoreis.wallets.WalletsRepository,
    ): SessionRepository = SessionRepositoryImpl(
        sessionDao = sessionDao,
        walletsRepository = walletsRepository
    )

    @Provides
    fun provideGetCurrentCurrencyCase(sessionRepository: SessionRepository): GetCurrentCurrencyCase = sessionRepository
}