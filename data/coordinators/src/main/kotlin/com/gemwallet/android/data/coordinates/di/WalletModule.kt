package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.gemwallet.android.data.coordinates.wallet.WalletIdGeneratorImpl
import com.gemwallet.android.data.repositoreis.transactions.TransactionRepository
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
    fun provideWalletIdGenerator(
        transactionRepository: TransactionRepository,
    ): WalletIdGenerator {
        return WalletIdGeneratorImpl()
    }
}