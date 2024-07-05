package com.gemwallet.android.data.swap

import com.gemwallet.android.blockchain.clients.SwapClient
import com.gemwallet.android.ext.toEVM
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.SwapQuoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.Gemstone.Config
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import java.math.BigInteger

class SwapRepository(
    private val gemApiClient: GemApiClient,
    private val swapClients: List<SwapClient>,
) {
    suspend fun getQuote(ownerAddress: String, from: AssetId, to: AssetId, amount: String, includeData: Boolean = false): SwapQuoteResult? {
        val quote = gemApiClient.getSwapQuote(
            GemApiClient.SwapRequest(
                fromAsset = from.toIdentifier(),
                toAsset = to.toIdentifier(),
                walletAddress = ownerAddress,
                amount = amount,
                includeData = includeData,
            )
        ).getOrNull() ?: return null
        val spender = quote.quote.approval?.spender ?: throw Exception("Approval data is null")
        swapClients.firstOrNull { it.isMaintain(from.chain) }?.checkSpender(spender)
        return quote
    }

    suspend fun getAllowance(assetId: AssetId, owner: String, spender: String): Boolean = withContext(Dispatchers.IO) {
        val client = swapClients.firstOrNull { assetId.chain == it.maintainChain() } ?: return@withContext true
        client.getAllowance(assetId, owner, spender) != BigInteger.ZERO
    }

    fun encodeApprove(spender: String): ByteArray {
        val function = EthereumAbiFunction("approve")
        function.addParamAddress(AnyAddress(spender, CoinType.ETHEREUM).data(), false)
        function.addParamUInt256(BigInteger.valueOf(Long.MAX_VALUE).toByteArray(), false)
        return EthereumAbi.encode(function)
    }
}