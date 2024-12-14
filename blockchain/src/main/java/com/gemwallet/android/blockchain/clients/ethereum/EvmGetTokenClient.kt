package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.has0xPrefix
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.EthereumAbiValue
import java.math.BigInteger

class EvmGetTokenClient(
    private val chain: Chain,
    private val callService: EvmCallService,
) : GetTokenClient {
    override suspend fun getTokenData(tokenId: String): Asset? = withContext(Dispatchers.IO) {
        val getNameJob = async { getERC20Name(tokenId) }
        val getSymbolJob = async { getERC20Symbol(tokenId) }
        val getDecimalsJob = async { getERC20Decimals(tokenId) }

        val name = getNameJob.await() ?: return@withContext null
        val symbol = getSymbolJob.await() ?: return@withContext null
        val decimals = getDecimalsJob.await() ?: return@withContext null

        Asset(
            id = AssetId(chain, tokenId),
            name = name,
            symbol = symbol,
            decimals = decimals.toInt(),
            type = AssetType.ERC20,
        )
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun getERC20Decimals(contract: String): BigInteger? {
        val data = EthereumAbi.encode(EthereumAbiFunction("decimals"))
        val params = mapOf(
            "to" to contract,
            "data" to data.toHexString(),
        )
        val request = JSONRpcRequest.create(
            EvmMethod.Call,
            listOf(
                params,
                "latest",
            ),
        )
        return callService.callNumber(request).getOrNull()?.result?.value
    }

    private suspend fun getERC20Name(contract: String): String? {
        val data = EthereumAbi.encode(EthereumAbiFunction("name"))
        val params = mapOf(
            "to" to contract,
            "data" to data.toHexString()
        )
        val request = JSONRpcRequest.create(
            EvmMethod.Call,
            listOf(
                params,
                "latest"
            )
        )
        val response = callService.callString(request).getOrNull()?.result ?: return null
        return decodeAbi(response)
    }

    private suspend fun getERC20Symbol(contract: String): String? {
        val data = EthereumAbi.encode(EthereumAbiFunction("symbol"))
        val params = mapOf(
            "to" to contract,
            "data" to data.toHexString()
        )
        val request = JSONRpcRequest.create(
            EvmMethod.Call,
            listOf(
                params,
                "latest"
            )
        )
        val response = callService.callString(request).getOrNull()?.result ?: return null
        return decodeAbi(response)
    }

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