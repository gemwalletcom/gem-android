package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.ext.toChainType
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import wallet.core.jni.Base58
import wallet.core.jni.StoredKey

class WCLoadPrivateDataOperator(
    private val keyStoreDir: String
) : LoadPrivateDataOperator {
    override suspend fun invoke(wallet: Wallet, password: String): String {
        val storeKey = StoredKey.load("$keyStoreDir/${wallet.id}")
        return if (wallet.type == WalletType.PrivateKey) {
            val bytes = storeKey.decryptPrivateKey(password.decodeHex())
            when (wallet.accounts.first().chain.toChainType()) {
                ChainType.Bitcoin, ChainType.Solana -> Base58.encodeNoCheck(bytes)
                else -> bytes.toHexString()
            }
        } else {
            storeKey.decryptMnemonic(password.decodeHex())
        }
    }

}