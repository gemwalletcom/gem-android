package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.blockchain.ton.TonBroadcastTransaction
import com.wallet.core.blockchain.ton.TonJettonBalance
import com.wallet.core.blockchain.ton.TonJettonToken
import com.wallet.core.blockchain.ton.TonMasterchainInfo
import com.wallet.core.blockchain.ton.TonMessageTransactions
import com.wallet.core.blockchain.ton.TonResult
import com.wallet.core.blockchain.ton.TonWalletInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.lang.reflect.Type

interface TonRpcClient {

    @GET("/api/v2/getWalletInformation")
    suspend fun walletInfo(@Query("address") address: String): Result<TonResult<TonWalletInfo>>

    @GET("/api/v2/getAddressState")
    suspend fun addressState(@Query("address") address: String): Result<TonResult<String>>

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