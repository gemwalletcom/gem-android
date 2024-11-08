package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SwapModule {
    @Singleton
    @Provides
    fun provideSwapRepository(
        signTransfer: SignTransfer,
        getCurrentNodeCase: GetCurrentNodeCase,
        passwordStore: PasswordStore,
        loadPrivateDataOperator: LoadPrivateKeyOperator,
    ): SwapRepository = SwapRepository(
        signClient = signTransfer,
        getCurrentNodeCase = getCurrentNodeCase,
        passwordStore = passwordStore,
        loadPrivateKeyOperator = loadPrivateDataOperator,
    )

}

