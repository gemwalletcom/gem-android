package com.gemwallet.android.interactors

import com.gemwallet.android.R
import com.gemwallet.android.blockchain.operators.InvalidPhrase
import com.gemwallet.android.blockchain.operators.InvalidWords
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.repositories.wallet.WalletsRepository
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.gemwallet.android.math.decodeHex
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import wallet.core.jni.PrivateKey

class PhraseAddressImportWalletOperator(
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val sessionRepository: SessionRepository,
    private val storePhraseOperator: StorePhraseOperator,
    private val phraseValidate: ValidatePhraseOperator,
    private val addressValidate: ValidateAddressOperator,
    private val passwordStore: PasswordStore,
    private val syncSubscription: SyncSubscription,
) : ImportWalletOperator {
    override suspend fun invoke(
        importType: ImportType,
        walletName: String,
        data: String,
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
        syncSubscription.invoke()
        assetsRepository.invalidateDefault(wallet, sessionRepository.getSession()?.currency ?: Currency.USD)
        sessionRepository.setWallet(wallet)
        return Result.success(wallet)
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
}