package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.data.repositoreis.config.SecurityGemPreferences
import com.gemwallet.android.data.repositoreis.config.SharedGemPreferences
import com.gemwallet.android.data.services.gemapi.NativeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import uniffi.gemstone.AlienProvider
import uniffi.gemstone.GemGateway
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object GatewayModule {

    @Singleton
    @Provides
    fun provideAlienProvider(
        getNodesCase: GetNodesCase,
        getCurrentNodeCase: GetCurrentNodeCase,
        setCurrentNodeCase: SetCurrentNodeCase,
        okHttpClient: OkHttpClient,
    ): AlienProvider {
        return NativeProvider(getNodesCase, getCurrentNodeCase, setCurrentNodeCase, okHttpClient)
    }

    @Provides
    @Singleton
    fun provideGateway(
        alienProvider: AlienProvider,
        @ApplicationContext context: Context,
    ): GemGateway {
        return GemGateway(
            alienProvider,
            preferences = SharedGemPreferences(
                sharedPreferences = context.getSharedPreferences("gateway_preferences", Context.MODE_PRIVATE)
            ),
            securePreferences = SecurityGemPreferences(context),
            apiUrl = "https://api.gemwallet.com"
        )
    }
}