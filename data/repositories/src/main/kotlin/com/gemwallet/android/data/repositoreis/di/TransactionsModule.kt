package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.application.transactions.coordinators.GetChangedTransactions
import com.gemwallet.android.application.transactions.coordinators.GetPendingTransactionsCount
import com.gemwallet.android.blockchain.services.TransactionStatusService
import com.gemwallet.android.cases.transactions.ClearPendingTransactions
import com.gemwallet.android.cases.transactions.CreateTransaction
import com.gemwallet.android.cases.transactions.GetTransaction
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.PutTransactions
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.transactions.SyncTransactionsService
import com.gemwallet.android.data.repositoreis.transactions.TransactionRepository
import com.gemwallet.android.data.repositoreis.transactions.TransactionsRepositoryImpl
import com.gemwallet.android.data.service.store.database.TransactionsDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemGateway
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TransactionsModule {

    @Singleton
    @Provides
    fun provideTransactionsRepository(
        transactionsDao: TransactionsDao,
        gateway: GemGateway,
    ): TransactionsRepositoryImpl = TransactionsRepositoryImpl(
        transactionsDao = transactionsDao,
        transactionStatusService = TransactionStatusService(
            gateway = gateway,
        ),
    )

    @Singleton
    @Provides
    fun provideTransactionRepository( // TODO: Remove when TransactionsRepositoryImpl will refactored
        impl: TransactionsRepositoryImpl
    ): TransactionRepository = impl

    @Singleton
    @Provides
    fun provideGetChangedTransactions(transactionsRepository: TransactionsRepositoryImpl): GetChangedTransactions {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideGetPendingTransactionsCount(transactionsRepository: TransactionsRepositoryImpl): GetPendingTransactionsCount {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideGetTransactionCase(transactionsRepository: TransactionsRepositoryImpl): GetTransaction {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun providePutTransactionsCase(transactionsRepository: TransactionsRepositoryImpl): PutTransactions {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideCreateTransactionsCase(transactionsRepository: TransactionsRepositoryImpl): CreateTransaction {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideUpdateTime(transactionsRepository: TransactionsRepositoryImpl): GetTransactionUpdateTime {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideClearPending(transactionsRepository: TransactionsRepositoryImpl): ClearPendingTransactions {
        return transactionsRepository
    }
    
    @Singleton
    @Provides
    fun syncTransactionsService(
        gemApiClient: GemApiClient,
        gemDeviceApiClient: GemDeviceApiClient,
        getDeviceId: GetDeviceId,
        putTransactions: PutTransactions,
        getTransactionUpdateTime: GetTransactionUpdateTime,
        assetsRepository: AssetsRepository,
    ): SyncTransactions {
        return SyncTransactionsService(
            gemDeviceApiClient = gemDeviceApiClient,
            putTransactions = putTransactions,
            getTransactionUpdateTime = getTransactionUpdateTime,
            assetsRepository = assetsRepository,
        )
    }
}

