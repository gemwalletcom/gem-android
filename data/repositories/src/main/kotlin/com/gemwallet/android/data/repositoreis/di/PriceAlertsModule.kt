package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.pricealerts.GetPriceAlerts
import com.gemwallet.android.cases.pricealerts.PutPriceAlert
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepositoryImpl
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
    fun providePriceAlertsRepositoryImpl(
        @ApplicationContext context: Context,
        gemClient: GemApiClient,
        priceAlertsDao: PriceAlertsDao,
        getDeviceId: GetDeviceId,
    ): PriceAlertRepositoryImpl {
        return PriceAlertRepositoryImpl(
            gemClient = gemClient,
            priceAlertsDao = priceAlertsDao,
            getDeviceId = getDeviceId,
            configStore = com.gemwallet.android.data.service.store.ConfigStore(
                context.getSharedPreferences(
                    "price-alerts",
                    Context.MODE_PRIVATE
                )
            ),
        )
    }

    @Provides
    @Singleton
    fun providePriceAlertsRepository(
        repository: PriceAlertRepositoryImpl
    ): PriceAlertRepository = repository

    @Provides
    fun provideGetPriceAlertsCase(repository: PriceAlertRepositoryImpl):  GetPriceAlerts = repository


    @Provides
    fun providePutPriceAlertCase(repository: PriceAlertRepositoryImpl):  PutPriceAlert = repository

    @Provides
    fun provideEnabledPriceAlertCase(repository: PriceAlertRepositoryImpl): EnablePriceAlert = repository
}