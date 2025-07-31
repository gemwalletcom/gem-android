package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.AddressStatusClientProxy
import com.gemwallet.android.blockchain.clients.tron.TronAddressStatusClient
import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.blockchain.operators.CreateWalletOperator
import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCCreateAccountOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCCreateWalletOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCDeleteKeyStoreOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCLoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCLoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCStorePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCValidateAddressOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCValidatePhraseOperator
import com.gemwallet.android.cases.banners.AddBanner
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.wallet.ImportWalletService
import com.gemwallet.android.data.password.PreferencePasswordStore
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.PhraseAddressImportWalletService
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.available
import com.wallet.core.primitives.Chain
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
    ): LoadPrivateDataOperator =
        WCLoadPrivateDataOperator(context.dataDir.toString())

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
        walletsRepository: WalletsRepository,
        assetsRepository: AssetsRepository,
        sessionRepository: SessionRepository,
        storePhraseOperator: StorePhraseOperator,
        phraseValidate: ValidatePhraseOperator,
        addressValidate: ValidateAddressOperator,
        passwordStore: PasswordStore,
        rpcClients: RpcClientAdapter,
        addBanner: AddBanner,
        syncSubscription: SyncSubscription,
    ): ImportWalletService = PhraseAddressImportWalletService(
        walletsRepository = walletsRepository,
        assetsRepository = assetsRepository,
        sessionRepository = sessionRepository,
        storePhraseOperator = storePhraseOperator,
        phraseValidate = phraseValidate,
        addressValidate = addressValidate,
        passwordStore = passwordStore,
        addBanner = addBanner,
        syncSubscription = syncSubscription,
        addressStatusClients = AddressStatusClientProxy(
            clients = Chain.available().mapNotNull {
                when (it) {
                    Chain.Tron -> TronAddressStatusClient(it, rpcClients.getClient(it))
                    else -> null
                }
            }
        )
    )
}