package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import com.gemwallet.android.data.repositoreis.config.UserConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ConfigModule {

    @Singleton
    @Provides
    fun provideConfigRepository(
        @ApplicationContext context: Context,
    ): UserConfig = UserConfig(context = context)

}

