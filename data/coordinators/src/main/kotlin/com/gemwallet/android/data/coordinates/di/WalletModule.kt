package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.application.wallet.coordinators.DeleteWallet
import com.gemwallet.android.application.wallet.coordinators.GetWalletDetails
import com.gemwallet.android.application.wallet.coordinators.GetWalletSecretData
import com.gemwallet.android.application.wallet.coordinators.SetWalletName
import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.data.coordinates.wallet.DeleteWalletImpl
import com.gemwallet.android.data.coordinates.wallet.GetWalletDetailsImpl
import com.gemwallet.android.data.coordinates.wallet.GetWalletSecretDataImpl
import com.gemwallet.android.data.coordinates.wallet.SetWalletNameImpl
import com.gemwallet.android.data.coordinates.wallet.WalletIdGeneratorImpl
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object WalletModule {
    @Provides
    @Singleton
    fun provideWalletIdGenerator(): WalletIdGenerator {
        return WalletIdGeneratorImpl()
    }

    @Provides
    @Singleton
    fun provideGetWalletDetails(
        walletsRepository: WalletsRepository
    ): GetWalletDetails {
        return GetWalletDetailsImpl(walletsRepository)
    }

    @Provides
    @Singleton
    fun provideSetWalletName(
        walletsRepository: WalletsRepository
    ): SetWalletName {
        return SetWalletNameImpl(walletsRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetWalletSecretData(
        walletsRepository: WalletsRepository,
        passwordStore: PasswordStore,
        loadPrivateDataOperator: LoadPrivateDataOperator,
    ): GetWalletSecretData {
        return GetWalletSecretDataImpl(
            walletsRepository = walletsRepository,
            passwordStore = passwordStore,
            loadPrivateDataOperator = loadPrivateDataOperator,
        )
    }

    @Provides
    fun provideDeleteWallet(
        sessionRepository: SessionRepository,
        walletsRepository: WalletsRepository,
        deleteKeyStoreOperator: DeleteKeyStoreOperator,
        syncSubscription: SyncSubscription,
    ): DeleteWallet {
        return DeleteWalletImpl(sessionRepository, walletsRepository, deleteKeyStoreOperator, syncSubscription)
    }
}