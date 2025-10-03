package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.services.WalletCoreSigner
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Chain
import uniffi.gemstone.HyperCore
import uniffi.gemstone.HyperCoreModelFactory
import java.math.BigInteger

class HyperCoreSignClient(
    val chain: Chain,
) : SignClient {

    private val hyperCore = HyperCore(WalletCoreSigner())
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
        val amount = params.asset.format(Crypto(finalAmount), decimalPlace = params.asset.decimals, showSymbol = false)
        return signSpotSend(
            amount = amount,
            destination = params.destination.address,
            token = nativeSpotToken,
            privateKey = privateKey
        )
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val amount = params.asset.format(Crypto(finalAmount), decimalPlace = params.asset.decimals, showSymbol = false) // TODO: Out to fun
        val (symbol, tokenId) = params.asset.id.tokenId?.split("::")?.takeIf { it.size >= 2 }
            ?.let { Pair(it[0], it[1]) } ?: throw IllegalArgumentException("Bad token")
        return signSpotSend(
            amount = amount,
            destination = params.destination.address,
            token = "$symbol:$tokenId",
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
        val signature = hyperCore.signSpotSend(spotSend, privateKey)
        return listOf(signature.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}