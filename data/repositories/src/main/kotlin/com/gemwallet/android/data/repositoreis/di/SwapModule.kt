package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.cases.swap.GetSwapQuotes
import com.gemwallet.android.cases.swap.GetSwapSupported
import com.gemwallet.android.data.repositoreis.swap.NativeProvider
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.AlienProvider
import uniffi.gemstone.GemSwapper
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SwapModule {

    @Singleton
    @Provides
    fun provideAlienProvider(
        getNodesCase: GetNodesCase,
        getCurrentNodeCase: GetCurrentNodeCase,
        setCurrentNodeCase: SetCurrentNodeCase,
    ): AlienProvider {
        return NativeProvider(getNodesCase, getCurrentNodeCase, setCurrentNodeCase)
    }

    @Singleton
    @Provides
    fun provideGemSwapper(
        alienProvider: AlienProvider,
    ) = GemSwapper(alienProvider)

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

    @Singleton
    @Provides
    fun provideGetSwapSupportCase(repository: SwapRepository): GetSwapSupported = repository

    @Singleton
    @Provides
    fun provideGetQuotesCase(repository: SwapRepository): GetSwapQuotes = repository
}

