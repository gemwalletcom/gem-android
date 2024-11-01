package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.services.gemapi.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object BuyModule {
    @Provides
    @Singleton
    fun provideBuyRepository(
        @ApplicationContext context: Context,
        gemFiatQuoteClient: GemApiClient,
    ): BuyRepository =
        BuyRepository(
            ConfigStore(
                context.getSharedPreferences(
                    "buy_config",
                    Context.MODE_PRIVATE
                )
            ),
            gemFiatQuoteClient
        )
}