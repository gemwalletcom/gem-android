package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.cases.wallet.DeleteWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.DeleteWalletOperator
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.cases.device.SyncSubscription
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object WalletsModule {

    @Provides
    fun provideDeleteWallet(
        sessionRepository: SessionRepository,
        walletsRepository: WalletsRepository,
        deleteKeyStoreOperator: DeleteKeyStoreOperator,
        syncSubscription: SyncSubscription,
    ): DeleteWallet {
        return DeleteWalletOperator(sessionRepository, walletsRepository, deleteKeyStoreOperator, syncSubscription)
    }

}

