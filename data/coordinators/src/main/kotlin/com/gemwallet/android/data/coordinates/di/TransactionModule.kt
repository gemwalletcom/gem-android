package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.transactions.coordinators.GetTransactionDetails
import com.gemwallet.android.application.transactions.coordinators.GetTransactions
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorer
import com.gemwallet.android.data.coordinates.transaction.GetTransactionDetailsImpl
import com.gemwallet.android.data.coordinates.transaction.GetTransactionsImpl
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
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
    @Provides
    @Singleton
    fun provideGetTransactionDetails(
        sessionRepository: SessionRepository,
        transactionRepository: TransactionRepository,
        assetsRepository: AssetsRepository,
        getCurrentBlockExplorer: GetCurrentBlockExplorer,
    ): GetTransactionDetails {
        return GetTransactionDetailsImpl(
            sessionRepository = sessionRepository,
            transactionRepository = transactionRepository,
            assetsRepository = assetsRepository,
            getCurrentBlockExplorer = getCurrentBlockExplorer,
        )
    }
}