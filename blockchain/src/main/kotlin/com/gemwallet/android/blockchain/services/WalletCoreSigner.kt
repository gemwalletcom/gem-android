package com.gemwallet.android.blockchain.services

import com.gemwallet.android.math.toHexString
import uniffi.gemstone.AlienSigner
import wallet.core.jni.Curve
import wallet.core.jni.EthereumAbi
import wallet.core.jni.PrivateKey

class WalletCoreSigner : AlienSigner {
    override fun signEip712(
        typedDataJson: String,
        privateKey: ByteArray
    ): String {
        val hash = EthereumAbi.encodeTyped(typedDataJson)
        val signature = PrivateKey(privateKey).sign(hash, Curve.SECP256K1)
        if (signature == null || signature.isEmpty()) throw Exception("Failed to sign")
        return signature.toHexString()
    }
}