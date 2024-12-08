package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ethereum.EvmRpcClient.EvmNumber
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.math.append0x
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.hexToBigInteger
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.blockchain.ethereum.models.EthereumFeeHistory
import com.wallet.core.blockchain.ethereum.models.EthereumTransactionReciept
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbiValue
import java.lang.reflect.Type
import java.math.BigInteger

interface EvmRpcClient {
    @POST("/")
    suspend fun getBalance(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>

    @POST("/")
    suspend fun getFeeHistory(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EthereumFeeHistory>>

    @POST("/")
    suspend fun getGasLimit(@Body request: JSONRpcRequest<List<Transaction>>): Result<JSONRpcResponse<EvmNumber?>>

    @POST("/")
    suspend fun getNetVersion(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>

    @POST("/")
    suspend fun getNonce(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>

    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>

    @POST("/")
    suspend fun callNumber(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmNumber?>>

    @POST("/")
    suspend fun callString(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String?>>

    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EthereumTransactionReciept>>

    @POST
    suspend fun chainId(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Response<JSONRpcResponse<EvmNumber?>>

    @POST
    suspend fun sync(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<Boolean?>>

    @POST
    suspend fun latestBlock(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>

    class EvmNumber(
        val value: BigInteger?,
    )

    class TokenBalance(
        val value: BigInteger?,
    )

    class Transaction(
        val from: String,
        val to: String,
        val value: String?,
        val data: String?,
    )

    class BalanceDeserializer : JsonDeserializer<EvmNumber> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): EvmNumber {
            return EvmNumber(
                try {
                    json?.asString?.hexToBigInteger()
                } catch (_: Throwable) {
                    null
                }
            )
        }
    }

    class TokenBalanceDeserializer : JsonDeserializer<TokenBalance> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): TokenBalance {
            return TokenBalance(
                try {
                    EthereumAbiValue.decodeUInt256(json?.asString?.decodeHex()).toBigIntegerOrNull()
                } catch (_: Throwable) {
                    null
                }
            )
        }

    }
}

internal suspend fun EvmRpcClient.getBalance(address: String): Result<JSONRpcResponse<EvmNumber?>> {
    return getBalance(
        JSONRpcRequest.create(
            method = EvmMethod.GetBalance,
            params = listOf(address, "latest")
        )
    )
}

internal suspend fun EvmRpcClient.callString(contract: String, hexData: String): String? {
    val params = mapOf(
        "to" to contract,
        "data" to hexData
    )
    val request = JSONRpcRequest.create(
        EvmMethod.Call,
        listOf(
            params,
            "latest"
        )
    )
    return callString(request).getOrNull()?.result
}

internal suspend fun EvmRpcClient.getChainId(url: String): Response<JSONRpcResponse<EvmNumber?>> {
    return chainId(url, JSONRpcRequest.create(EvmMethod.GetChainId, emptyList()))
}

internal suspend fun EvmRpcClient.latestBlock(url: String): Result<JSONRpcResponse<EvmNumber?>> {
    return latestBlock(url, JSONRpcRequest.create(EvmMethod.GetBlockNumber, emptyList()))
}

internal suspend fun EvmRpcClient.sync(url: String): Result<JSONRpcResponse<Boolean?>> {
    return sync(url, JSONRpcRequest.create(EvmMethod.Sync, emptyList()))
}

internal suspend fun EvmRpcClient.getFeeHistory(): EthereumFeeHistory? {
    return getFeeHistory(JSONRpcRequest.create(EvmMethod.GetFeeHistory, listOf("10", "latest", listOf(25))))
        .getOrNull()?.result
}

internal suspend fun EvmRpcClient.getNetVersion(coinType: CoinType): BigInteger {
    return try {
        getNetVersion(JSONRpcRequest.create(EvmMethod.GetNetVersion, emptyList()))
            .fold({ it.result?.value }) { null } ?: BigInteger(coinType.chainId())
    } catch (_: Throwable) {
        return BigInteger(coinType.chainId())
    }
}

internal suspend fun EvmRpcClient.getNonce(fromAddress: String): BigInteger {
    val nonceParams = listOf(fromAddress, "latest")
    return getNonce(JSONRpcRequest.create(EvmMethod.GetNonce, nonceParams))
        .fold({ it.result?.value }) { null } ?: BigInteger.ZERO
}

internal suspend fun EvmRpcClient.getGasLimit(from: String, to: String, amount: BigInteger, data: String?): BigInteger {
    val transaction = EvmRpcClient.Transaction(
        from = from,
        to = to,
        value = "0x${amount.toString(16)}",
        data = if (data.isNullOrEmpty()) "0x" else data.append0x(),
    )
    val request = JSONRpcRequest.create(EvmMethod.GetGasLimit, listOf(transaction))
    return getGasLimit(request).fold({ it.result?.value ?: BigInteger.ZERO}) {
        BigInteger.ZERO
    }
}