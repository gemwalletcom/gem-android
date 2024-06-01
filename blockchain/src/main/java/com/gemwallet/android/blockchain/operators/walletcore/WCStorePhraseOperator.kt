package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.StorePhraseOperator
import com.gemwallet.android.math.decodeHex
import wallet.core.jni.CoinType
import wallet.core.jni.StoredKey

class WCStorePhraseOperator(
    private val keyStoreDir: String,
) : StorePhraseOperator {
    override suspend fun invoke(walletId: String, mnemonic: String, password: String): Result<Boolean> = try {
        val storedKey = StoredKey.importHDWallet(
            mnemonic,
            walletId,
            password.decodeHex(),
            CoinType.BITCOIN
        )
        storedKey.store("$keyStoreDir/$walletId")
        Result.success(true)
    } catch (err: Throwable) {
        Result.failure(err)
    }
}