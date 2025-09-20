package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.services.TransactionStatusService
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.transactions.ClearPendingTransactions
import com.gemwallet.android.cases.transactions.CreateTransaction
import com.gemwallet.android.cases.transactions.GetTransaction
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.cases.transactions.PutTransactions
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.transactions.SyncTransactionsService
import com.gemwallet.android.data.repositoreis.transactions.TransactionsRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.TransactionsDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
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
        assetsDao: AssetsDao,
        gateway: GemGateway,
        rpcClients: RpcClientAdapter,
    ): TransactionsRepository = TransactionsRepository(
        transactionsDao = transactionsDao,
        assetsDao = assetsDao,
        transactionStatusService = TransactionStatusService(
            gateway = gateway,
        ),
    )

    @Singleton
    @Provides
    fun provideGetTransactionsCase(transactionsRepository: TransactionsRepository): GetTransactions {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideGetTransactionCase(transactionsRepository: TransactionsRepository): GetTransaction {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun providePutTransactionsCase(transactionsRepository: TransactionsRepository): PutTransactions {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideCreateTransactionsCase(transactionsRepository: TransactionsRepository): CreateTransaction {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideUpdateTime(transactionsRepository: TransactionsRepository): GetTransactionUpdateTime {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideClearPending(transactionsRepository: TransactionsRepository): ClearPendingTransactions {
        return transactionsRepository
    }
    
    @Singleton
    @Provides
    fun syncTransactionsService(
        gemApiClient: GemApiClient,
        sessionRepository: SessionRepository,
        getDeviceIdCase: GetDeviceIdCase,
        putTransactions: PutTransactions,
        getTransactionUpdateTime: GetTransactionUpdateTime,
        assetsRepository: AssetsRepository,
    ): SyncTransactions {
        return SyncTransactionsService(
            gemApiClient = gemApiClient,
            sessionRepository = sessionRepository,
            getDeviceIdCase = getDeviceIdCase,
            putTransactions = putTransactions,
            getTransactionUpdateTime = getTransactionUpdateTime,
            assetsRepository = assetsRepository,
        )
    }
}

