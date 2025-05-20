package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.batch
import com.gemwallet.android.blockchain.clients.ethereum.services.createCallRequest
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.has0xPrefix
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.EthereumAbiValue

class EvmGetTokenClient(
    private val chain: Chain,
    private val callService: EvmCallService,
) : GetTokenClient {
    override suspend fun getTokenData(tokenId: String): Asset? = withContext(Dispatchers.IO) {
        val params = listOf(
            callService.createCallRequest(tokenId, EthereumAbi.encode(EthereumAbiFunction("name")).toHexString(), "latest"),
            callService.createCallRequest(tokenId, EthereumAbi.encode(EthereumAbiFunction("symbol")).toHexString(), "latest"),
            callService.createCallRequest(tokenId, EthereumAbi.encode(EthereumAbiFunction("decimals")).toHexString(), "latest"),
        )
        val result = callService.batch(params)
        val name = result.getOrNull(0)?.let { decodeAbi(it) }
        val symbol = result.getOrNull(1)?.let { decodeAbi(it) }
        val decimals = result.getOrNull(2)?.let { it.hexToBigInteger()?.toInt() }

        if (name.isNullOrEmpty() || symbol.isNullOrEmpty() || decimals == null) {
            null
        } else {
            Asset(
                id = AssetId(chain, tokenId),
                name = name,
                symbol = symbol,
                decimals = decimals,
                type = AssetType.ERC20,
            )
        }
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun decodeAbi(hexString: String): String? {
        val data = hexString.decodeHex()
        if (data.size < 32) {
            return null
        }
        return EthereumAbiValue.decodeValue(data.drop(32).toByteArray(), "string")
    }

    companion object {
        fun isTokenAddress(tokenId: String): Boolean {
            return tokenId.has0xPrefix() && tokenId.isNotEmpty() && tokenId.length == 42
        }
    }
}