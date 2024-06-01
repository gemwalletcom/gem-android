package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.math.decodeHex
import com.wallet.core.primitives.Chain
import wallet.core.jni.Derivation
import wallet.core.jni.StoredKey

class WCLoadPrivateKeyOperator(
    private val keyStoreDir: String,
) : LoadPrivateKeyOperator {
    override suspend fun invoke(walletId: String, chain: Chain, password: String): ByteArray {
        val coinType = WCChainTypeProxy().invoke(chain)
        val store = StoredKey.load("$keyStoreDir/$walletId")
        val wallet = store.wallet(password.decodeHex())
        val privateKey = when (chain) {
            Chain.Solana -> wallet.getKeyDerivation(coinType, Derivation.SOLANASOLANA)
            else -> wallet.getKey(coinType, coinType.derivationPath())
        }
        return privateKey.data()
    }
}