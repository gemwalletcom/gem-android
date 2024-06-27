package com.gemwallet.android.interactors

import com.gemwallet.android.R
import com.gemwallet.android.blockchain.operators.InvalidPhrase
import com.gemwallet.android.blockchain.operators.InvalidWords
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

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
        }
        if (result.isFailure) {
            return result
        }
        val wallet = result.getOrNull() ?: return Result.failure(Exception("Unknown error"))
        syncSubscription.invoke()
        assetsRepository.invalidateDefault(wallet.type, wallet, sessionRepository.getSession()?.currency ?: Currency.USD)
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
        val result = walletsRepository.addPhrase(walletName, cleanedData, importType.walletType, importType.chain)
        val wallet = result.getOrNull()
        return if (result.isFailure || wallet == null) {
            result
        } else {
            val password = passwordStore.createPassword(wallet.id)
            val storeResult = storePhraseOperator(wallet.id, cleanedData, password)
            if (storeResult.isSuccess) {
                result
            } else {
                walletsRepository.removeWallet(wallet.id)
                Result.failure(storeResult.exceptionOrNull() ?: ImportError.CreateError("Unknown error"))
            }
        }
    }

    private suspend fun handleAddress(chain: Chain, walletName: String, data: String): Result<Wallet> {
        if (addressValidate(data, chain).getOrNull() != true) {
            R.string.errors_create_wallet
            return Result.failure(ImportError.InvalidAddress)
        }
        val result = walletsRepository.addWatch(walletName, data, chain)
        val wallet = result.getOrNull()

        return if (result.isFailure || wallet == null) {
            Result.failure(ImportError.CreateError(result.exceptionOrNull()?.message ?: "Unknown error"))
        } else {
            Result.success(wallet)
        }
    }

}