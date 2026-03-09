package com.gemwallet.android.cases.wallet

import com.gemwallet.android.model.ImportType
import com.wallet.core.primitives.Wallet

interface ImportWalletService {
    suspend fun importWallet(
        importType: ImportType,
        walletName: String,
        data: String,
    ): Wallet

    suspend fun createWallet(
        walletName: String,
        data: String,
    ): Wallet
}

sealed class ImportError(message: String = "") : Exception(message) {

    object InvalidationSecretPhrase : ImportError()

    object InvalidationPrivateKey : ImportError()

    class InvalidWords(val words: List<String>) : ImportError()

    object InvalidAddress : ImportError()

    class CreateError(message: String) : ImportError(message)

    class DuplicatedWallet(val wallet: Wallet) : ImportError("Duplicated wallet")
}