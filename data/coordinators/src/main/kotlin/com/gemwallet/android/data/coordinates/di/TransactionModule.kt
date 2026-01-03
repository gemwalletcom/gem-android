package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.transactions.coordinators.GetTransactions
import com.gemwallet.android.data.coordinates.transaction.GetTransactionsImpl
import com.gemwallet.android.data.repositoreis.transactions.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TransactionModule {
    @Provides
    @Singleton
    fun provideGetTransactions(
        transactionRepository: TransactionRepository,
    ): GetTransactions {
        return GetTransactionsImpl(transactionRepository)
    }
}