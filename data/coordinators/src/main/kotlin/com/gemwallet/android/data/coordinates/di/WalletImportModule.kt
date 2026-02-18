package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.wallet_import.coordinators.GetImportWalletState
import com.gemwallet.android.application.wallet_import.services.ImportAssets
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.coordinates.wallet_import.services.ImportWalletService
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object WalletImportModule {

    @Provides
    @Singleton
    fun provideImportAssetsService(
        sessionRepository: SessionRepository,
        gemDeviceApiClient: GemDeviceApiClient,
        searchTokensCase: SearchTokensCase,
        assetsRepository: AssetsRepository,
    ): ImportWalletService {
        return ImportWalletService(
            sessionRepository = sessionRepository,
            gemDeviceApiClient = gemDeviceApiClient,
            searchTokensCase = searchTokensCase,
            assetsRepository = assetsRepository,
        )
    }

    @Provides
    fun provideImportAssets(service: ImportWalletService): ImportAssets = service

    @Provides
    fun provideGetImportWalletState(service: ImportWalletService): GetImportWalletState = service
}