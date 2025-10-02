package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.format
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Chain
import uniffi.gemstone.HyperCore
import uniffi.gemstone.HyperCoreModelFactory
import wallet.core.jni.Curve
import wallet.core.jni.EthereumAbi
import wallet.core.jni.PrivateKey
import java.math.BigInteger

class HyperCoreSignClient(
    val chain: Chain,
) : SignClient {

    private val hyperCore = HyperCore()
    private val factory = HyperCoreModelFactory()
//    private val agentNamePrefix = "gemwallet_"
//    private val referralCode = "GEMWALLET"
//    private val builderAddress = "0x0d9dab1a248f63b0a48965ba8435e4de7497a3dc"
    private val nativeSpotToken = "HYPE:0x0d01dc56dcaaca66ad901c959b4011ec"

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val amount = params.asset.format(Crypto(finalAmount), decimalPlace = -1)
        return signSpotSend(
            amount = amount,
            destination = params.destination.address,
            token = nativeSpotToken,
            privateKey = privateKey
        )
    }

    private fun signSpotSend(
        amount: String,
        destination: String,
        token: String,
        privateKey: ByteArray
    ): List<ByteArray> {
        val timestamp = System.currentTimeMillis().toULong()
        val spotSend = factory.sendSpotTokenToAddress(
            amount = amount,
            destination = destination,
            time = timestamp,
            token = token
        )
        val eip712Message = hyperCore.sendSpotTokenToAddressTypedData(spotSend)
        val signature = signEIP712(eip712Message, privateKey)
        return listOf(actionMessage(signature, eip712Message, timestamp).toByteArray())
    }

    private fun signEIP712(messageJson: String, privateKey: ByteArray): String {
        val hash = EthereumAbi.encodeTyped(messageJson)
        val signature = PrivateKey(privateKey).sign(hash, Curve.SECP256K1)
        if (signature == null || signature.isEmpty()) throw Exception("Failed to sign")
        return signature.toHexString()
    }

    private fun actionMessage(signature: String, eip712Message: String, timestamp: ULong): String {
        val eip712Json = jsonEncoder.decodeFromString<Map<String, String>>(eip712Message)
        return factory.buildSignedRequest(
            signature = signature,
            action = eip712Json["message"] ?: throw IllegalStateException("Sign error"),
            timestamp = timestamp
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}