package com.gemwallet.android.data.services.gemapi.di

import android.content.Context
import android.os.Build
import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.data.services.gemapi.Mime
import com.gemwallet.android.data.services.gemapi.http.SecurityInterceptor
import com.gemwallet.android.model.BuildInfo
import com.gemwallet.android.serializer.jsonEncoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ClientsModule {

    @Provides
    @Singleton
    fun provideSecurityInterceptor(getDeviceId: GetDeviceId) = SecurityInterceptor(getDeviceId)

    @Provides
    @Singleton
    fun provideGemHttpClient(
        @ApplicationContext context: Context,
        buildInfo: BuildInfo,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .cache(Cache(context.cacheDir, 10 * 1024 * 1024))
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .addNetworkInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("User-Agent", "Gem/${buildInfo.versionCode}  Android/${Build.VERSION.RELEASE} Version/${buildInfo.versionName}")
                    .build()
            )
        }
        .build()

    @Provides
    @Singleton
    fun provideGemApiClient(httpClient: OkHttpClient): GemApiClient =
        Retrofit.Builder()
            .baseUrl("https://api.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(jsonEncoder.asConverterFactory(Mime.Json.value))
            .build()
            .create(GemApiClient::class.java)

    @Provides
    @Singleton
    fun provideGemDeviceApiClient(
        httpClient: OkHttpClient,
        securityInterceptor: SecurityInterceptor,
    ): GemDeviceApiClient {
        val httpClient = httpClient.newBuilder()
            .addNetworkInterceptor(securityInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(jsonEncoder.asConverterFactory(Mime.Json.value))
            .build()
            .create(GemDeviceApiClient::class.java)
    }

    @Provides
    @Singleton
    fun provideGemApiStaticClient(httpClient: OkHttpClient): GemApiStaticClient {
        return Retrofit.Builder()
            .baseUrl("https://assets.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(jsonEncoder.asConverterFactory(Mime.Json.value))
            .build()
            .create(GemApiStaticClient::class.java)
    }
}