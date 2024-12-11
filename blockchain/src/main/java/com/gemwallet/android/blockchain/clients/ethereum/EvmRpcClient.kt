package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ethereum.EvmRpcClient.EvmNumber
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmBalancesService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmFeeService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.hexToBigInteger
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.blockchain.ethereum.models.EthereumTransactionReciept
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import wallet.core.jni.EthereumAbiValue
import java.lang.reflect.Type
import java.math.BigInteger

interface EvmRpcClient :
    EvmCallService,
    EvmBalancesService,
    EvmFeeService
{

    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>

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

internal suspend fun EvmRpcClient.getChainId(url: String): Response<JSONRpcResponse<EvmNumber?>> {
    return chainId(url, JSONRpcRequest.create(EvmMethod.GetChainId, emptyList()))
}

internal suspend fun EvmRpcClient.latestBlock(url: String): Result<JSONRpcResponse<EvmNumber?>> {
    return latestBlock(url, JSONRpcRequest.create(EvmMethod.GetBlockNumber, emptyList()))
}

internal suspend fun EvmRpcClient.sync(url: String): Result<JSONRpcResponse<Boolean?>> {
    return sync(url, JSONRpcRequest.create(EvmMethod.Sync, emptyList()))
}