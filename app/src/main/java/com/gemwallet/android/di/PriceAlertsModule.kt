package com.gemwallet.android.di

import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.database.PriceAlertsDao
import com.gemwallet.android.data.repositories.pricealerts.PriceAlertRepository
import com.gemwallet.android.services.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PriceAlertsModule {

    @Provides
    @Singleton
    fun providePriceAlertsRepository(
        gemClient: GemApiClient,
        priceAlertsDao: PriceAlertsDao,
        configRepository: ConfigRepository,
    ): PriceAlertRepository {
        return PriceAlertRepository(
            gemClient = gemClient,
            priceAlertsDao = priceAlertsDao,
            configRepository = configRepository,
        )
    }

    @Provides
    fun provideGetPriceAlertsCase(repository: PriceAlertRepository):  GetPriceAlertsCase = repository


    @Provides
    fun providePutPriceAlertCase(repository: PriceAlertRepository):  PutPriceAlertCase = repository

    @Provides
    fun provideEnabledPriceAlertCase(repository: PriceAlertRepository): EnablePriceAlertCase = repository
}