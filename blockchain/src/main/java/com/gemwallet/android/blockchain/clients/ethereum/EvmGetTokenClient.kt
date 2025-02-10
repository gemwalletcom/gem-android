package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.batch
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
            mapOf(
                "to" to tokenId,
                "data" to EthereumAbi.encode(EthereumAbiFunction("decimals")).toHexString(),
            ),
            mapOf(
                "to" to tokenId,
                "data" to EthereumAbi.encode(EthereumAbiFunction("symbol")).toHexString(),
            ),
            mapOf(
                "to" to tokenId,
                "data" to EthereumAbi.encode(EthereumAbiFunction("name")).toHexString()
            ),
        )
        val result = callService.batch(params)
        Asset(
            id = AssetId(chain, tokenId),
            name = decodeAbi(result[2]) ?: return@withContext null,
            symbol = decodeAbi(result[1]) ?: return@withContext null,
            decimals = result[0].hexToBigInteger()?.toInt() ?: return@withContext null,
            type = AssetType.ERC20,
        )
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