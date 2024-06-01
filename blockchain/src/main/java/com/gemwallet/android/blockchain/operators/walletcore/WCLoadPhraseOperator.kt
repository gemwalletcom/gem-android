package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.LoadPhraseOperator
import com.gemwallet.android.math.decodeHex
import wallet.core.jni.StoredKey

class WCLoadPhraseOperator(
    private val keyStoreDir: String
) : LoadPhraseOperator {
    override suspend fun invoke(walletId: String, password: String): String {
        return StoredKey.load("$keyStoreDir/$walletId")
            .decryptMnemonic(password.decodeHex())
    }

}