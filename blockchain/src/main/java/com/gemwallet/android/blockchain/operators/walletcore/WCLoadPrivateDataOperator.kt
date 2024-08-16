package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import wallet.core.jni.StoredKey

class WCLoadPrivateDataOperator(
    private val keyStoreDir: String
) : LoadPrivateDataOperator {
    override suspend fun invoke(wallet: Wallet, password: String): String {
        val storeKey = StoredKey.load("$keyStoreDir/${wallet.id}")
        return if (wallet.type == WalletType.private_key)
            storeKey.decryptPrivateKey(password.decodeHex()).toHexString()
        else
            storeKey.decryptMnemonic(password.decodeHex())
    }

}