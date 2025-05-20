package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import java.io.File

class WCDeleteKeyStoreOperator(
    private val keyStoreDir: String,
    private val passwordStore: PasswordStore,
) : DeleteKeyStoreOperator {

    override fun invoke(walletId: String): Boolean {
        if (!passwordStore.removePassword(walletId)) {
            return false
        }
        if (!File("$keyStoreDir/$walletId").delete()) {
            return false
        }
        return true
    }
}