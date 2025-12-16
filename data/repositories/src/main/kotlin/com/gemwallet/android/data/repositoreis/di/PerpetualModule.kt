package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepositoryImpl
import com.gemwallet.android.data.service.store.database.PerpetualBalanceDao
import com.gemwallet.android.data.service.store.database.PerpetualDao
import com.gemwallet.android.data.service.store.database.PerpetualPositionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PerpetualModule {

    @Provides
    @Singleton
    fun providePerpetualRepository(
        perpetualDao: PerpetualDao,
        perpetualBalanceDao: PerpetualBalanceDao,
        perpetualPositionDao: PerpetualPositionDao,
    ): PerpetualRepository {
        return PerpetualRepositoryImpl(
            perpetualDao =  perpetualDao,
            perpetualBalanceDao = perpetualBalanceDao,
            perpetualPositionDao = perpetualPositionDao,
        )
    }
}