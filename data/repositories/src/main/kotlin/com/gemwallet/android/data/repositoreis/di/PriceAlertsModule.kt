package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepositoryImpl
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
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
        priceAlertsDao: PriceAlertsDao,
    ): PriceAlertRepository {
        return PriceAlertRepositoryImpl(
            context = context,
            priceAlertsDao = priceAlertsDao,
            configStore = com.gemwallet.android.data.service.store.ConfigStore(
                context.getSharedPreferences(
                    "price-alerts",
                    Context.MODE_PRIVATE
                )
            ),
        )
    }
}