package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.pricealerts.coordinators.AddPriceAlert
import com.gemwallet.android.application.pricealerts.coordinators.GetPriceAlerts
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.coordinates.pricealerts.AddPriceAlertImpl
import com.gemwallet.android.data.coordinates.pricealerts.GetPriceAlertsImpl
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PriceAlertModule {
    @Provides
    @Singleton
    fun provideAddPriceAlerts(
        gemApiClient: GemApiClient,
        getDeviceId: GetDeviceId,
        priceAlertRepository: PriceAlertRepository,
    ): AddPriceAlert {
        return AddPriceAlertImpl(
            gemApiClient = gemApiClient,
            getDeviceIdImpl = getDeviceId,
            priceAlertRepository = priceAlertRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPriceAlerts(
        priceAlertRepository: PriceAlertRepository,
        assetsRepository: AssetsRepository,
    ): GetPriceAlerts {
        return GetPriceAlertsImpl(
            priceAlertRepository = priceAlertRepository,
            assetsRepository = assetsRepository,
        )
    }
}