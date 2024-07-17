package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.SignerParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.Base58
import wallet.core.jni.Base64
import wallet.core.jni.proto.NEAR
import java.io.ByteArrayOutputStream
import java.math.BigInteger

class NearSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransfer(params: SignerParams, privateKey: ByteArray): ByteArray {
        val metadata = params.info as NearSignerPreloader.Info

        val input = NEAR.SigningInput.newBuilder().apply {
            this.signerId = params.owner
            this.nonce = metadata.sequence
            this.receiverId = params.input.destination()?.address
            this.addAllActions(
                listOf(
                    NEAR.Action.newBuilder().apply {
                        this.transfer = NEAR.Transfer.newBuilder().apply {
                            this.deposit = ByteString.copyFrom(params.finalAmount.littleIndian()?.reversedArray())
                        }.build()
                    }.build()
                )
            )
            this.blockHash = ByteString.copyFrom(Base58.decodeNoCheck(metadata.block))
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(input, WCChainTypeProxy().invoke(chain), NEAR.SigningOutput.parser())
        return Base64.encode(output.signedTransaction.toByteArray()).toByteArray()
    }

    override fun maintainChain(): Chain = chain
}

private fun BigInteger.littleIndian(): ByteArray? {
    val isPositive = signum() == 1 || signum() == 0
    val byteCount = 128 / 8
    val valueData = twos()
    if (valueData.size > byteCount) {
        return null
    }
    val data = mutableListOf<ByteArray>()
    if (isPositive) {
        data.add(ByteArray(byteCount - valueData.size) { 0 })
    } else {
        data.add(ByteArray(byteCount - valueData.size) { 0xff.toByte() })
    }
    data.add(valueData)
    val outBuff = ByteArrayOutputStream()
    data.forEach {
        outBuff.write(it, 0, it.size)
    }
    return outBuff.toByteArray()
}

private fun BigInteger.twos(): ByteArray {
    val contents = toByteArray()
    if (signum() == 1 || signum() == 0) {
        return contents
    }
    val padding = 0xff.toByte()
    val padded = ByteArray(32)
    for (i in padded.indices) {
        padded[i] = padding
    }
    System.arraycopy(
        contents,
        0,
        padded,
        32 - contents.size,
        contents.size
    )
    return padded
}