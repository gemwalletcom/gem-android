package com.gemwallet.android.data.swap

import com.gemwallet.android.blockchain.clients.SwapClient
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.SwapQuoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import java.math.BigInteger

class SwapRepository(
    private val gemApiClient: GemApiClient,
    private val swapClients: List<SwapClient>,
) {
    private val oneinch = "0x1111111254EEB25477B68fb85Ed929f73A960582"

    suspend fun getQuote(ownerAddress: String, from: AssetId, to: AssetId, amount: String, includeData: Boolean = false): SwapQuoteResult? {
        return gemApiClient.getSwapQuote(
            GemApiClient.SwapRequest(
                fromAsset = from.toIdentifier(),
                toAsset = to.toIdentifier(),
                walletAddress = ownerAddress,
                amount = amount,
                includeData = includeData,
            )
        ).getOrNull()
    }

    suspend fun getAllowance(assetId: AssetId, owner: String): Boolean = withContext(Dispatchers.IO) {
        val client = swapClients.firstOrNull { assetId.chain == it.maintainChain() } ?: return@withContext true
        client.getAllowance(assetId, owner, oneinch) != BigInteger.ZERO
    }

    suspend fun encodeApprove(): ByteArray {
        val function = EthereumAbiFunction("approve")
        function.addParamAddress(AnyAddress(oneinch, CoinType.ETHEREUM).data(), false)
        function.addParamUInt256(BigInteger.valueOf(Long.MAX_VALUE).toByteArray(), false)
        return EthereumAbi.encode(function)
    }
}