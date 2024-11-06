package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.cases.banners.AddBannerCase
import com.gemwallet.android.cases.banners.CancelBannerCase
import com.gemwallet.android.cases.banners.GetBannersCase
import com.gemwallet.android.data.repositoreis.banners.BannersRepository
import com.gemwallet.android.data.repositoreis.config.ConfigRepository
import com.gemwallet.android.data.service.store.database.BannersDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object BannersModule {
    @Singleton
    @Provides
    fun provideBannersRepository(
        bannersDao: BannersDao,
        configRepository: ConfigRepository,
    ): BannersRepository {
        return BannersRepository(
            bannersDao,
            configRepository
        )
    }

    @Singleton
    @Provides
    fun provideGetBannersCase(bannersRepository: BannersRepository): GetBannersCase = bannersRepository

    @Singleton
    @Provides
    fun provideCancelBannerCase(bannersRepository: BannersRepository): CancelBannerCase = bannersRepository

    @Singleton
    @Provides
    fun provideAddBannerCase(bannersRepository: BannersRepository): AddBannerCase = bannersRepository
}

