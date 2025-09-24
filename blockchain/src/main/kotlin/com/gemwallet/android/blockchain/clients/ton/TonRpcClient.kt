package com.gemwallet.android.blockchain.clients.ton

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
import kotlinx.serialization.SerialName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.lang.reflect.Type

interface TonRpcClient {

    @GET("/api/v2/getAddressBalance")
    suspend fun balance(@Query("address") address: String): Result<TonResult<String>>

    @GET("/api/v2/getWalletInformation")
    suspend fun walletInfo(@Query("address") address: String): Result<TonResult<TonWalletInfo>>

    @POST("/api/v2/sendBocReturnHash")
    suspend fun broadcast(@Body boc: Boc): Result<TonResult<TonBroadcastTransaction>>

    @GET("/api/v3/transactionsByMessage")
    suspend fun transaction(@Query("msg_hash") hash: String): Result<TonMessageTransactions>

    @GET("/api/v2/getTokenData")
    suspend fun tokenData(@Query("address") address: String): Result<TonResult<TonJettonToken>>

    @GET("/api/v3/jetton/wallets?limit=100&offset=0")
    suspend fun getJettonWallets(@Query("owner_address") ownerAddress: String): JettonWalletsResponse

    @GET("/api/v2/getTokenData")
    suspend fun tokenBalance(@Query("address") address: String): Result<TonResult<TonJettonBalance>>

    @GET("/api/v2/getAddressState")
    suspend fun addressState(@Query("address") address: String): Result<TonResult<String>>

    @GET//("/api/v2/getMasterchainInfo")
    suspend fun masterChainInfo(@Url string: String): Response<TonResult<TonMasterchainInfo>>

    data class Boc(
        val boc: String
    )

    data class JetonAddress(
        val b64: String,
        val len: Long,
    )

    data class JettonWalletsResponse(
        val jetton_wallets: List<JettonWallet>
    )

    data class JettonWallet(
        val address: String,
        @SerialName("deserialize_biguint_from_str") val balance: String,
        val jetton: String,
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