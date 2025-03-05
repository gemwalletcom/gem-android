package com.gemwallet.android.data.repositoreis.di

import android.content.Context
import android.os.Environment
import com.gemwallet.android.cases.update.CheckForUpdateCase
import com.gemwallet.android.cases.update.DownloadLatestApkCase
import com.gemwallet.android.cases.update.ObserveUpdateDownloadCase
import com.gemwallet.android.cases.update.SkipVersionCase
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.update.AppUpdateRepository
import com.gemwallet.android.data.services.files.FileService
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.model.AppUpdateConfig
import com.gemwallet.android.model.BuildInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppUpdateModule {

    @Singleton
    @Provides
    fun provideAppUpdateRepository(
        @ApplicationContext context: Context,
        buildInfo: BuildInfo,
        appUpdateConfig: AppUpdateConfig,
        userConfig: UserConfig,
        gemApiClient: GemApiClient,
        fileService: FileService
    ): AppUpdateRepository {
        val apkDirectory =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir

        return AppUpdateRepository(
            apkDirectory.toString(),
            buildInfo,
            appUpdateConfig,
            userConfig,
            gemApiClient,
            fileService
        )
    }

    @Singleton
    @Provides
    fun provideCheckForUpdateCase(appUpdateRepository: AppUpdateRepository): CheckForUpdateCase {
        return appUpdateRepository
    }

    @Singleton
    @Provides
    fun provideDownloadLatestApkCase(appUpdateRepository: AppUpdateRepository): DownloadLatestApkCase {
        return appUpdateRepository
    }

    @Singleton
    @Provides
    fun provideObserveUpdateDownloadCase(appUpdateRepository: AppUpdateRepository): ObserveUpdateDownloadCase {
        return appUpdateRepository
    }

    @Singleton
    @Provides
    fun provideSkipVersionCase(appUpdateRepository: AppUpdateRepository): SkipVersionCase {
        return appUpdateRepository
    }
}
