package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.blockchain.operators.CreateWalletOperator
import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.blockchain.operators.LoadPhraseOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCCreateAccountOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCCreateWalletOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCDeleteKeyStoreOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCLoadPhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCLoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCStorePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCValidateAddressOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCValidatePhraseOperator
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.password.PreferencePasswordStore
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.interactors.ImportWalletOperator
import com.gemwallet.android.interactors.PhraseAddressImportWalletOperator
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.gemwallet.android.services.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object InteractsModule {

    @Singleton
    @Provides
    fun provideValidateAddressInteract(): ValidateAddressOperator = WCValidateAddressOperator()

    @Singleton
    @Provides
    fun provideValidatePhraseInteract(): ValidatePhraseOperator = WCValidatePhraseOperator()

    @Singleton
    @Provides
    fun provideCreateWalletInteract(): CreateWalletOperator = WCCreateWalletOperator()

    @Singleton
    @Provides
    fun provideCreateAccountInteract(): CreateAccountOperator = WCCreateAccountOperator()

    @Singleton
    @Provides
    fun provideStorePhraseInteract(
        @ApplicationContext context: Context
    ): StorePhraseOperator =
        WCStorePhraseOperator(context.dataDir.toString())

    @Singleton
    @Provides
    fun provideLoadPhraseInteract(
        @ApplicationContext context: Context
    ): LoadPhraseOperator =
        WCLoadPhraseOperator(context.dataDir.toString())

    @Singleton
    @Provides
    fun provideLoadPrivateKeyInteract(
        @ApplicationContext context: Context,
    ): LoadPrivateKeyOperator = WCLoadPrivateKeyOperator(context.dataDir.toString())

    @Singleton
    @Provides
    fun provideDeleteKeyStoreOperator(
        @ApplicationContext context: Context,
        passwordStore: PasswordStore,
    ): DeleteKeyStoreOperator = WCDeleteKeyStoreOperator(context.dataDir.toString(), passwordStore)

    @Provides
    @Singleton
    fun providePasswordStore(@ApplicationContext context: Context): PasswordStore =
        PreferencePasswordStore(context)

    @Singleton
    @Provides
    fun provideAddWalletInteract(
        gemApiClient: GemApiClient,
        configRepository: ConfigRepository,
        walletsRepository: WalletsRepository,
        assetsRepository: AssetsRepository,
        sessionRepository: SessionRepository,
        storePhraseOperator: StorePhraseOperator,
        phraseValidate: ValidatePhraseOperator,
        addressValidate: ValidateAddressOperator,
        passwordStore: PasswordStore,
    ): ImportWalletOperator = PhraseAddressImportWalletOperator(
        walletsRepository = walletsRepository,
        assetsRepository = assetsRepository,
        sessionRepository = sessionRepository,
        storePhraseOperator = storePhraseOperator,
        phraseValidate = phraseValidate,
        addressValidate = addressValidate,
        passwordStore = passwordStore,
        syncSubscription = SyncSubscription(gemApiClient, walletsRepository, configRepository),
    )
}