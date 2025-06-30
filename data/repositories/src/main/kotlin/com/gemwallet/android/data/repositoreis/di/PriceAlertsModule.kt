package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.pricealerts.GetPriceAlerts
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
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
        getDeviceIdCase: GetDeviceIdCase,
    ): PriceAlertRepository {
        return PriceAlertRepository(
            gemClient = gemClient,
            priceAlertsDao = priceAlertsDao,
            getDeviceIdCase = getDeviceIdCase,
            configStore = com.gemwallet.android.data.service.store.ConfigStore(
                context.getSharedPreferences(
                    "price-alerts",
                    Context.MODE_PRIVATE
                )
            ),
        )
    }

    @Provides
    fun provideGetPriceAlertsCase(repository: PriceAlertRepository):  GetPriceAlerts = repository


    @Provides
    fun providePutPriceAlertCase(repository: PriceAlertRepository):  PutPriceAlertCase = repository

    @Provides
    fun provideEnabledPriceAlertCase(repository: PriceAlertRepository): EnablePriceAlert = repository
}