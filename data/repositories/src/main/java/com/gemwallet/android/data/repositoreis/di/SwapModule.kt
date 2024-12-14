package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.clients.SignClientProxy
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.data.repositoreis.swap.NativeProvider
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemSwapper
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SwapModule {

    @Singleton
    @Provides
    fun provideGemSwapper(
        getCurrentNodeCase: GetCurrentNodeCase,
    ) = GemSwapper(NativeProvider(getCurrentNodeCase))

    @Singleton
    @Provides
    fun provideSwapRepository(
        gemSwapper: GemSwapper,
        signClient: SignClientProxy,
        passwordStore: PasswordStore,
        loadPrivateDataOperator: LoadPrivateKeyOperator,
    ): SwapRepository = SwapRepository(
        gemSwapper = gemSwapper,
        signClient = signClient,
        passwordStore = passwordStore,
        loadPrivateKeyOperator = loadPrivateDataOperator,
    )
}

