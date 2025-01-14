package com.gemwallet.android.interactors

import com.gemwallet.android.R
import com.gemwallet.android.blockchain.clients.AddressStatusClientProxy
import com.gemwallet.android.blockchain.operators.InvalidPhrase
import com.gemwallet.android.blockchain.operators.InvalidWords
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.cases.banners.AddBannerCase
import com.gemwallet.android.cases.device.SyncSubscriptionCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.AddressStatus
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.PrivateKey

class PhraseAddressImportWalletOperator(
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val sessionRepository: SessionRepository,
    private val storePhraseOperator: StorePhraseOperator,
    private val phraseValidate: ValidatePhraseOperator,
    private val addressValidate: ValidateAddressOperator,
    private val passwordStore: PasswordStore,
    private val syncSubscriptionCase: SyncSubscriptionCase,
    private val addressStatusClients: AddressStatusClientProxy,
    private val addBannerCase: AddBannerCase,
) : ImportWalletOperator {
    override suspend fun importWallet(
        importType: ImportType,
        walletName: String,
        data: String
    ): Result<Wallet> {
        val result = when (importType.walletType) {
            WalletType.multicoin -> handlePhrase(importType, walletName, data)
            WalletType.single -> handlePhrase(importType, walletName, data)
            WalletType.view -> handleAddress(importType.chain!!, walletName, data)
            WalletType.private_key -> handlePrivateKey(importType.chain!!, walletName, data)
        }
        if (result.isFailure) {
            return result
        }
        val wallet = result.getOrNull() ?: return Result.failure(Exception("Unknown error"))

        syncSubscriptionCase.syncSubscription(listOf(wallet))
        assetsRepository.createAssets(wallet)
        assetsRepository.importAssets(wallet, sessionRepository.getSession()?.currency ?: Currency.USD)
        checkAddresses(wallet)
        sessionRepository.setWallet(wallet)
        return Result.success(wallet)
    }

    override suspend fun createWallet(walletName: String, data: String): Result<Wallet> = withContext(Dispatchers.IO) {
        val result = handlePhrase(ImportType(WalletType.multicoin), walletName, data)
        if (result.isFailure) return@withContext result

        val wallet = result.getOrNull() ?: return@withContext Result.failure(Exception("Unknown error"))

        async { syncSubscriptionCase.syncSubscription(listOf(wallet)) }
        assetsRepository.createAssets(wallet)
        sessionRepository.setWallet(wallet)
        Result.success(wallet)
    }

    private suspend fun handlePhrase(importType: ImportType, walletName: String, rawData: String): Result<Wallet> {
        val cleanedData = rawData.trim().split("\\s+".toRegex()).joinToString(" ") { it.trim() }
        val validateResult = phraseValidate(cleanedData)
        if (validateResult.isFailure || validateResult.getOrNull() != true) {
            val error = validateResult.exceptionOrNull() ?: InvalidPhrase
            return when (error) {
                is InvalidWords -> Result.failure(ImportError.InvalidWords(error.words))
                else -> Result.failure(ImportError.InvalidationSecretPhrase)
            }
        }
        val wallet = walletsRepository.addControlled(walletName, cleanedData, importType.walletType, importType.chain)
        val password = passwordStore.createPassword(wallet.id)
        val storeResult = storePhraseOperator(wallet, cleanedData, password)
        return if (storeResult.isSuccess) {
            Result.success(wallet)
        } else {
            walletsRepository.removeWallet(wallet.id)
            Result.failure(storeResult.exceptionOrNull() ?: ImportError.CreateError("Unknown error"))
        }
    }

    private suspend fun handleAddress(chain: Chain, walletName: String, data: String): Result<Wallet> {
        if (addressValidate(data, chain).getOrNull() != true) {
            R.string.errors_create_wallet
            return Result.failure(ImportError.InvalidAddress)
        }
        return try {
            val wallet = walletsRepository.addWatch(walletName, data, chain)
            Result.success(wallet)
        } catch (err: Exception) {
            Result.failure(ImportError.CreateError(err.message ?: "Unknown error"))
        }
    }

    private suspend fun handlePrivateKey(chain: Chain, walletName: String, data: String): Result<Wallet> {
        val cleanedData = data.trim()
        try {
            if (!PrivateKey.isValid(cleanedData.decodeHex(), WCChainTypeProxy().invoke(chain).curve())) {
                throw Exception()
            }
        } catch (_: Throwable) {
            return Result.failure(ImportError.InvalidationPrivateKey)
        }
        val wallet = walletsRepository.addControlled(walletName, cleanedData, WalletType.private_key, chain)
        val password = passwordStore.createPassword(wallet.id)
        val storeResult = storePhraseOperator(wallet, cleanedData, password)
        return if (storeResult.isSuccess) {
            Result.success(wallet)
        } else {
            walletsRepository.removeWallet(wallet.id)
            Result.failure(storeResult.exceptionOrNull() ?: ImportError.CreateError("Unknown error"))
        }
    }

    private suspend fun checkAddresses(wallet: Wallet) {
        wallet.accounts.forEach {
            val statuses = addressStatusClients.getAddressStatus(it.chain, it.address)
            for (status in statuses) {
                if (status == AddressStatus.MultiSignature) {
                    addBannerCase.addBanner(
                        wallet = wallet,
                        chain = it.chain,
                        event = BannerEvent.AccountBlockedMultiSignature,
                        state = BannerState.AlwaysActive,
                    )
                }
            }
        }
    }
}