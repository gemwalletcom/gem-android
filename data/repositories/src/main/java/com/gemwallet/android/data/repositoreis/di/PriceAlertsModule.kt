package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.repositoreis.config.ConfigRepository
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
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
            configStore = com.gemwallet.android.data.service.store.ConfigStore(
                context.getSharedPreferences(
                    "price-alerts",
                    Context.MODE_PRIVATE
                )
            ),
        )
    }

    @Provides
    fun provideGetPriceAlertsCase(repository: PriceAlertRepository):  GetPriceAlertsCase = repository


    @Provides
    fun providePutPriceAlertCase(repository: PriceAlertRepository):  PutPriceAlertCase = repository

    @Provides
    fun provideEnabledPriceAlertCase(repository: PriceAlertRepository): EnablePriceAlertCase = repository
}