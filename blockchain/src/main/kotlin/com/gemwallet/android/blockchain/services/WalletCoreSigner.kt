package com.gemwallet.android.blockchain.services

import uniffi.gemstone.AlienSigner

class WalletCoreSigner : AlienSigner {
    override fun signEip712(
        typedDataJson: String,
        privateKey: ByteArray
    ): String {
        TODO("Not yet implemented")
    }
}