package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.math.decodeHex
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import wallet.core.jni.Derivation
import wallet.core.jni.PrivateKey
import wallet.core.jni.StoredKey

class WCLoadPrivateKeyOperator(
    private val keyStoreDir: String,
) : LoadPrivateKeyOperator {
    override suspend fun invoke(wallet: Wallet, chain: Chain, password: String): ByteArray {
        val coinType = WCChainTypeProxy().invoke(chain)
        val store = StoredKey.load("$keyStoreDir/${wallet.id}")
        val privateKey = if (wallet.type == WalletType.private_key) {
            PrivateKey(store.decryptPrivateKey(password.decodeHex()))
        } else {
            store.wallet(password.decodeHex()).let {
                when (chain) {
                    Chain.Solana -> it.getKeyDerivation(coinType, Derivation.SOLANASOLANA)
                    else -> it.getKey(coinType, coinType.derivationPath())
                }
            }
        }
        return privateKey.data()
    }
}