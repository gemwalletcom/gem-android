package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.data.repositoreis.config.SecurityGemPreferences
import com.gemwallet.android.data.repositoreis.config.SharedGemPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.AlienProvider
import uniffi.gemstone.GemGateway
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object GatewayModule {
    @Provides
    @Singleton
    fun provideGateway(
        alienProvider: AlienProvider,
        @ApplicationContext context: Context,
    ): GemGateway {
        return GemGateway(
            alienProvider,
            SharedGemPreferences(
                context.getSharedPreferences("gateway_preferences", Context.MODE_PRIVATE)
            ),
            SecurityGemPreferences(context)
        )
    }
}