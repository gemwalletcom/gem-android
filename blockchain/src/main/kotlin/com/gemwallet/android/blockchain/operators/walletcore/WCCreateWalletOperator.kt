package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.CreateWalletOperator
import wallet.core.jni.HDWallet

class WCCreateWalletOperator : CreateWalletOperator {
    override suspend fun invoke(): Result<String> =
        Result.success(HDWallet(128, "").mnemonic())
}