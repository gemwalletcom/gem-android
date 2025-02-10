package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import wallet.core.jni.Base64
import wallet.core.jni.Curve
import wallet.core.jni.PrivateKey
import java.math.BigInteger

class SuiSignClient(
    private val chain: Chain,
) : SignClient {

    override suspend fun sign(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = chainData as SuiSignerPreloader.SuiChainData
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