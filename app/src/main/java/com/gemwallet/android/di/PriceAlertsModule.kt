package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.database.PriceAlertsDao
import com.gemwallet.android.data.repositories.config.ConfigStore
import com.gemwallet.android.data.repositories.pricealerts.PriceAlertRepository
import com.gemwallet.android.services.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PriceAlertsModule {

    @Provides
    @Singleton
    fun providePriceAlertsRepository(
        @ApplicationContext context: Context,
        gemClient: GemApiClient,
        priceAlertsDao: PriceAlertsDao,
        configRepository: ConfigRepository,
    ): PriceAlertRepository {
        return PriceAlertRepository(
            gemClient = gemClient,
            priceAlertsDao = priceAlertsDao,
            configRepository = configRepository,
            configStore = ConfigStore(context.getSharedPreferences("price-alerts", Context.MODE_PRIVATE)),
        )
    }

    @Provides
    fun provideGetPriceAlertsCase(repository: PriceAlertRepository):  GetPriceAlertsCase = repository


    @Provides
    fun providePutPriceAlertCase(repository: PriceAlertRepository):  PutPriceAlertCase = repository

    @Provides
    fun provideEnabledPriceAlertCase(repository: PriceAlertRepository): EnablePriceAlertCase = repository
}