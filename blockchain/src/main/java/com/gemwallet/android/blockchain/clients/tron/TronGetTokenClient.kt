package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.AnyAddress
import wallet.core.jni.Base58
import wallet.core.jni.EthereumAbiValue
import java.math.BigInteger

class TronGetTokenClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : GetTokenClient {
    override suspend fun getTokenData(tokenId: String): Asset? = withContext(Dispatchers.IO) {
        if (!AnyAddress.isValid(tokenId, WCChainTypeProxy().invoke(chain))) {
            return@withContext null
        }
        val getName = async { getName(tokenId) }
        val getSymbol = async { getSymbol(tokenId) }
        val getDecimals = async { getDecimals(tokenId) }
        val name = getName.await() ?: return@withContext null
        val symbol = getSymbol.await()  ?: return@withContext null
        val decimals = getDecimals.await()  ?: return@withContext null

        Asset(
            id = AssetId(chain, tokenId),
            name = name,
            symbol = symbol,
            decimals = decimals,
            type = AssetType.TRC20,
        )
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain

    private suspend fun getName(contract: String): String? {
        return getTokenString(contract, "name()")
    }

    private suspend fun getSymbol(contract: String): String? {
        return getTokenString(contract, "symbol()")
    }

    private suspend fun getDecimals(contract: String): Int? {
        val result = smartContractCallFunction(contract, "decimals()") ?: return null
        val decimal = BigInteger(result, 16)
        return decimal.toInt()
    }

    private suspend fun getTokenString(contract: String, function: String): String? {
        val result = smartContractCallFunction(contract, function) ?: return null
        return decodeAbi(result)
    }

    private suspend fun smartContractCallFunction(contract: String, function: String): String? {
        val result = rpcClient.triggerSmartContract(
            contractAddress = "41" + Base58.decode(contract).toHexString("").drop(2),
            functionSelector = function,
            parameter = null,
            feeLimit = null,
            callValue = null,
            ownerAddress = contract,
            visible = null,
        ).getOrNull()
        return result?.constant_result?.firstOrNull()
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
            return tokenId.startsWith("T") && tokenId.length in 30..50
        }
    }
}