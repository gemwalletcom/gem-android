package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepositoryImpl
import com.gemwallet.android.data.service.store.database.AccountsDao
import com.gemwallet.android.data.service.store.database.WalletsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object WalletsModule {

    @Provides
    fun provideWalletsRepository(
        walletsDao: WalletsDao,
        accountsDao: AccountsDao,
        createAccountOperator: CreateAccountOperator,
        walletIdGenerator: WalletIdGenerator,
    ): WalletsRepository {
        return WalletsRepositoryImpl(
            walletsDao = walletsDao,
            accountsDao = accountsDao,
            createAccount = createAccountOperator,
            walletIdGenerator = walletIdGenerator,
        )
    }

}

