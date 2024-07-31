package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.blockchain.ton.models.TonBroadcastTransaction
import com.wallet.core.blockchain.ton.models.TonJettonBalance
import com.wallet.core.blockchain.ton.models.TonJettonToken
import com.wallet.core.blockchain.ton.models.TonMasterchainInfo
import com.wallet.core.blockchain.ton.models.TonResult
import com.wallet.core.blockchain.ton.models.TonTransactionMessage
import com.wallet.core.blockchain.ton.models.TonWalletInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.lang.reflect.Type

interface TonRpcClient {

    @GET("/api/v2/getAddressBalance")
    suspend fun balance(@Query("address") address: String): Result<TonResult<String>>

    @GET("/api/v2/getWalletInformation")
    suspend fun walletInfo(@Query("address") address: String): Result<TonResult<TonWalletInfo>>

    @POST("/api/v2/sendBocReturnHash")
    suspend fun broadcast(@Body boc: Boc): Result<TonResult<TonBroadcastTransaction>>

    @GET("/api/index/v1/getTransactionsByInMessageHash")
    suspend fun transaction(@Query("msg_hash") hash: String): Result<List<TonTransactionMessage>>

    @GET("/api/v2/getTokenData")
    suspend fun tokenData(@Query("address") address: String): Result<TonResult<TonJettonToken>>

    @POST("/api/v2/jsonRPC")
    suspend fun getJetonAddress(@Body request: JSONRpcRequest<Any>): Result<JetonAddress?>

    @GET("/api/v2/getTokenData")
    suspend fun tokenBalance(@Query("address") address: String): Result<TonResult<TonJettonBalance>>

    @GET("/api/v2/getAddressState")
    suspend fun addressState(@Query("address") address: String): Result<TonResult<String>>

    @GET("/api/v2/getMasterchainInfo")
    suspend fun masterChainInfo(): Result<TonResult<TonMasterchainInfo>>

    data class Boc(
        val boc: String
    )

    data class JetonAddress(
        val b64: String,
        val len: Long,
    )

    class JetonAddressSerializer : JsonDeserializer<JetonAddress?> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): JetonAddress? {
            val jObj = json?.asJsonObject ?: return null
            val data = jObj
                .getAsJsonObject("result")
                .getAsJsonArray("stack")
                .get(0).asJsonArray[1]?.asJsonObject
                ?.getAsJsonObject("object")
                ?.getAsJsonObject("data") ?: return null
            return JetonAddress(
                b64 = data.get("b64")?.asString ?: return null,
                len = data.get("len")?.asLong ?: return null,
            )
        }

    }

}