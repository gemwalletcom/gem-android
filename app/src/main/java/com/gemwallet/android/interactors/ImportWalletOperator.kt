package com.gemwallet.android.interactors

import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.wallet.core.primitives.Wallet

interface ImportWalletOperator {
    suspend operator fun invoke(
        importType: ImportType,
        walletName: String,
        data: String,
    ): Result<Wallet>
}

sealed class ImportError(message: String = "") : Exception(message) {

    data object InvalidationSecretPhrase : ImportError()

    data object InvalidationPrivateKey : ImportError()

    class InvalidWords(val words: List<String>) : ImportError()

    data object InvalidAddress : ImportError()

    class CreateError(message: String) : ImportError(message)
}