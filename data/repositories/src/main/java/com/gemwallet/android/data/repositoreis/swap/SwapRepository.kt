package com.gemwallet.android.data.repositoreis.swap

import com.gemwallet.android.blockchain.clients.SwapClient
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.models.SwapRequest
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SwapQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class SwapRepository(
    private val gemApiClient: GemApiClient,
    private val swapClients: List<SwapClient>,
) {
    suspend fun getQuote(ownerAddress: String, from: AssetId, to: AssetId, amount: String, includeData: Boolean = false): SwapQuote? {
        val result = gemApiClient.getSwapQuote(
            SwapRequest(
                fromAsset = from.toIdentifier(),
                toAsset = to.toIdentifier(),
                walletAddress = ownerAddress,
                amount = amount,
                includeData = includeData,
            )
        )
        val quote = result.getOrNull()?.quote ?: return null
        val spender = quote.approval?.spender
        if (!includeData) {
            swapClients.firstOrNull { it.isMaintain(from.chain) }?.checkSpender(spender ?: "")
        }

        return quote
    }

    suspend fun getAllowance(assetId: AssetId, owner: String, spender: String): BigInteger = withContext(Dispatchers.IO) {
        val client = swapClients.firstOrNull { assetId.chain == it.maintainChain() } ?: return@withContext BigInteger.ZERO
        client.getAllowance(assetId, owner, spender)
    }

    fun isRequestApprove(chain: Chain): Boolean = swapClients
        .firstOrNull { chain == it.maintainChain() }?.isRequestApprove() == true
}