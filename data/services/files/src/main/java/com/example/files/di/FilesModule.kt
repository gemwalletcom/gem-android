package com.example.files.di

import com.example.files.FileService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FilesModule {

    @Singleton
    @Provides
    fun provideFileService(
        @Named("DownloadFileHttpClient") okHttpClient: OkHttpClient
    ): FileService {
        return FileService(okHttpClient)
    }

    @Named("DownloadFileHttpClient")
    @Singleton
    @Provides
    fun provideDownloadFileHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }
}
