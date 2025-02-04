package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import wallet.core.jni.Base64
import wallet.core.jni.Curve
import wallet.core.jni.PrivateKey

class SuiSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransaction(params: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): List<ByteArray> {
        val metadata = params.chainData as SuiSignerPreloader.SuiChainData
        return signTxDataDigest(metadata.messageBytes, privateKey)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun signTxDataDigest(data: String, privateKey: ByteArray): List<ByteArray> {
        val key = PrivateKey(privateKey)
        val pubKey = key.publicKeyEd25519
        val parts = data.split("_")
        val digest = parts[1].decodeHex()
        val signature = key.sign(digest, Curve.ED25519)
        val sig = byteArrayOf(0x0) + signature + pubKey.data()
        return listOf("${parts[0]}_${Base64.encode(sig)}".toByteArray())
    }
}