package com.gemwallet.android.services

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import com.gemwallet.android.ext.findByString
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.Charts
import com.wallet.core.primitives.ConfigResponse
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.FiatQuotes
import com.wallet.core.primitives.NameProvider
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import com.wallet.core.primitives.Platform
import com.wallet.core.primitives.PlatformStore
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.Release
import com.wallet.core.primitives.Subscription
import com.wallet.core.primitives.SwapApprovalData
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.SwapQuote
import com.wallet.core.primitives.SwapQuoteData
import com.wallet.core.primitives.SwapQuoteResult
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionInput
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.reflect.Type

class Transactions : ArrayList<Transaction>()

interface GemApiClient {
    @GET("/v1/config")
    suspend fun getConfig(): Result<ConfigResponse>

    @POST("/v1/prices")
    suspend fun prices(@Body request: AssetPricesRequest): Result<PricesResponse>

    @GET("/v1/fiat/on_ramp/quotes/{asset_id}")
    suspend fun getQuote(
        @Path("asset_id") assetId: String,
        @Query("amount") amount: Double,
        @Query("currency") currency: String,
        @Query("wallet_address") owner: String,
    ): Result<FiatQuotes>

    @GET("/v1/fiat/on_ramp/assets")
    suspend fun getFiatAssets(): Result<FiatAssets>

    @GET("/v1/transactions/by_device_id/{device_id}")
    suspend fun getTransactions(
        @Path("device_id") deviceId: String,
        @Query("wallet_index") walletIndex: Int,
        @Query("from_timestamp") from: Long
    ): Result<Transactions>

    @GET("/v1/name/resolve/{domain}")
    suspend fun resolve(@Path("domain") domain: String, @Query("chain") chain: String): Result<NameRecord>

    @POST("/v1/devices")
    suspend fun registerDevice(@Body request: Device): Result<Device>

    @GET("/v1/devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): Result<Device>

    @PUT("/v1/devices/{device_id}")
    suspend fun updateDevice(@Path("device_id") deviceId: String, @Body request: Device): Result<Device>

    @GET("/v1/subscriptions/{device_id}")
    suspend fun getSubscriptions(@Path("device_id") deviceId: String): Result<List<Subscription>>

    @HTTP(method = "DELETE", path = "/v1/subscriptions/{device_id}", hasBody = true)
    suspend fun deleteSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): Result<Any>

    @POST("/v1/subscriptions/{device_id}")
    suspend fun addSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): Result<Any>

    @GET("/v1/charts/{asset_id}")
    suspend fun getChart(@Path("asset_id") assetId: String, @Query("currency") currency: String, @Query("period") period: String): Result<Charts>

    @GET("/v1/assets/{asset_id}")
    suspend fun getAsset(@Path("asset_id") assetId: String, @Query("currency") currency: String): Result<AssetFull>

    @POST("/v1/swap/quote")
    suspend fun getSwapQuote(@Body request: SwapRequest): Result<SwapQuoteResult>

    @GET("/v1/assets/search")
    suspend fun search(
        @Query("query") query: String,
    ): Result<List<AssetFull>>

    @GET("/v1/assets/by_device_id/{device_id}")
    suspend fun getAssets(@Path("device_id") deviceId: String, @Query("wallet_index") walletIndex: Int, @Query("from_timestamp") fromTimestamp: Int = 0): Result<List<String>>

    @POST("/v1/price_alerts/{device_id}")
    suspend fun includePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): Result<String>

    @HTTP(method = "DELETE", path = "/v1/price_alerts/{device_id}", hasBody = true)
    suspend fun excludePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): Result<String>

    @GET("/v1/price_alerts/{device_id}")
    suspend fun getPriceAlerts(@Path("device_id") deviceId: String): Result<List<PriceAlert>>

    data class PricesResponse(
        val currency: String,
        val prices: List<AssetPrice> = emptyList()
    )

    data class SwapRequest(
        val fromAsset: String,
        val toAsset: String,
        val walletAddress: String,
        val amount: String,
        val includeData: Boolean,
    )

    class NodeSerializer : JsonDeserializer<Node>, JsonSerializer<Node> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Node {
            val jObj = json?.asJsonObject ?: throw IllegalArgumentException()
            val status = jObj["status"].asString.lowercase()
            return Node(
                url = jObj["url"].asString,
                priority = jObj["priority"].asInt,
                status = NodeState.entries.firstOrNull { it.string == status }
                    ?: throw IllegalArgumentException()
            )
        }

        override fun serialize(
            src: Node,
            typeOfSrc: Type,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonObject().apply {
                addProperty("url", src.url)
                addProperty("priority", src.priority)
                addProperty("status", src.status.string)
            }
        }
    }

    class SwapQuoteDeserializer : JsonDeserializer<SwapQuote> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): SwapQuote {
            val jObj = json?.asJsonObject ?: throw IllegalArgumentException()
            val data = if (jObj["data"].isJsonNull) {
                null
            } else if (jObj["data"].isJsonObject) {
                val jData = jObj["data"].asJsonObject
                SwapQuoteData(
                    to = jData["to"].asString,
                    value = jData["value"].asString,
                    data = jData["data"].asString,
                )
            } else {
                null
            }
            val approval = if (jObj["approval"] == null || jObj["approval"].isJsonNull
                || !jObj["approval"].isJsonObject
                || jObj["approval"].asJsonObject["spender"].isJsonNull
                || !jObj["approval"].asJsonObject["spender"].isJsonPrimitive) {
                null
            } else {
                SwapApprovalData(spender = jObj["approval"].asJsonObject["spender"].asString)
            }
            return SwapQuote(
                chainType = ChainType.entries.firstOrNull { it.string == jObj.get("chainType").asString } ?: throw JsonSyntaxException("Unsupported chain"),
                fromAmount = jObj["fromAmount"].asString,
                toAmount = jObj["toAmount"].asString,
                feePercent = jObj["feePercent"].asFloat,
                provider = SwapProvider(
                    name = jObj["provider"].asJsonObject.get("name").asString,
                ),
                data = data,
                approval = approval,
            )
        }
    }

    class DeviceSerializer : JsonDeserializer<Device>, JsonSerializer<Device> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Device {
            val jObj = json?.asJsonObject ?: throw JsonSyntaxException(json?.toString())
            val platformStore = if (jObj["platformStore"].isJsonNull) null else jObj["platformStore"]?.asString
            return Device(
                id = jObj["id"]?.asString ?: "",
                platform = when (jObj["platform"]?.asString) {
                    Platform.IOS.string -> Platform.IOS
                    else -> Platform.Android
                },
                platformStore = PlatformStore.entries.firstOrNull { it.string == platformStore },
                isPriceAlertsEnabled = jObj["isPriceAlertsEnabled"]?.asBoolean == true,
                token = jObj["token"]?.asString ?: "",
                locale = jObj["locale"]?.asString ?: "",
                version = jObj["version"]?.asString ?: "",
                isPushEnabled = jObj["isPushEnabled"]?.asBoolean ?: false,
                currency = jObj["currency"]?.asString ?: Currency.USD.string,
                subscriptionsVersion = jObj["subscriptionsVersion"]?.asInt ?: 0,
            )
        }

        override fun serialize(
            src: Device,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonObject().apply {
                addProperty("id", src.id)
                addProperty("platform", src.platform.string)
                addProperty("platformStore", src.platformStore?.string)
                addProperty("token", src.token)
                addProperty("locale", src.locale)
                addProperty("version", src.version)
                addProperty("isPushEnabled", src.isPushEnabled)
                addProperty("currency", src.currency)
                addProperty("subscriptionsVersion", src.subscriptionsVersion)
                addProperty("isPriceAlertsEnabled", src.isPriceAlertsEnabled)
            }
        }

    }

    class SubscriptionSerializer : JsonDeserializer<Subscription>, JsonSerializer<Subscription> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Subscription {
            val jObj = json.asJsonObject ?: throw JsonSyntaxException(json.toString())
            val chainString = jObj["chain"]?.asString ?: throw JsonSyntaxException(json.toString())
            val chain = Chain.entries.firstOrNull {
                chainString == it.string
            } ?: throw JsonSyntaxException(json.toString())
            return Subscription(
                address = jObj["address"]?.asString ?: throw JsonSyntaxException(json.toString()),
                chain = chain,
                wallet_index = jObj["wallet_index"]?.asInt ?: throw JsonSyntaxException(json.toString()),
            )
        }

        override fun serialize(
            src: Subscription,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonObject().apply {
                addProperty("address", src.address)
                addProperty("chain", src.chain.string)
                addProperty("wallet_index", src.wallet_index)
            }
        }
    }

    class TransactionsSerializer : JsonDeserializer<Transactions> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Transactions {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            format.timeZone = TimeZone.getTimeZone("GMT")
            val jArr = json.asJsonArray ?: throw JsonSyntaxException(json.toString())
            val result = Transactions()
            jArr.mapNotNull {
                TransactionDeserialize.toTransaction(it, format)
            }.forEach {
                result.add(it)
            }
            return result
        }
    }

    class TransactionDeserialize : JsonDeserializer<Transaction?> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext?
        ): Transaction? {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            format.timeZone = TimeZone.getTimeZone("GMT")
            return toTransaction(json, format)
        }
        companion object {
            fun toTransaction(jsonElement: JsonElement, format: DateFormat): Transaction? {
                val jObj = jsonElement.asJsonObject
                val assetIdJson = jObj["assetId"].asJsonObject
                val feeAssetIdJson = jObj["feeAssetId"].asJsonObject
                val assetId = AssetId(
                    chain = Chain.findByString(assetIdJson["chain"].asString ?: return null) ?: return null,
                    tokenId = if (assetIdJson["tokenId"].isJsonNull) null else assetIdJson["tokenId"].asString,
                )
                val feeAssetId = AssetId(
                    chain = Chain.findByString(feeAssetIdJson["chain"].asString ?: return null) ?: return null,
                    tokenId = if (feeAssetIdJson["tokenId"].isJsonNull) null else feeAssetIdJson["tokenId"].asString,
                )
                return Transaction(
                    id = jObj["id"].asString,
                    hash = jObj["hash"].asString,
                    assetId = assetId,
                    from = jObj["from"].asString,
                    to = jObj["to"].asString,
                    contract = if (jObj["contract"].isJsonNull) null else jObj["contract"].asString,
                    type = when (jObj["type"].asString) {
                        TransactionType.Transfer.string -> TransactionType.Transfer
                        TransactionType.Swap.string -> TransactionType.Swap
                        TransactionType.TokenApproval.string -> TransactionType.TokenApproval
                        TransactionType.StakeDelegate.string -> TransactionType.StakeDelegate
                        TransactionType.StakeRedelegate.string -> TransactionType.StakeRedelegate
                        TransactionType.StakeRewards.string -> TransactionType.StakeRewards
                        TransactionType.StakeUndelegate.string -> TransactionType.StakeUndelegate
                        TransactionType.StakeWithdraw.string -> TransactionType.StakeWithdraw
                        else -> return null
                    },
                    state = when (jObj["state"].asString) {
                        TransactionState.Pending.string -> TransactionState.Pending
                        TransactionState.Confirmed.string -> TransactionState.Confirmed
                        TransactionState.Reverted.string -> TransactionState.Reverted
                        TransactionState.Failed.string -> TransactionState.Failed
                        else -> return null
                    },
                    blockNumber = jObj["blockNumber"].asString,
                    sequence = jObj["sequence"].asString,
                    fee = jObj["fee"].asString,
                    feeAssetId = feeAssetId,
                    value = jObj["value"].asString,
                    memo = if (jObj["memo"].isJsonNull) null else jObj["memo"].asString,
                    direction = when (jObj["direction"].asString) {
                        TransactionDirection.Outgoing.string -> TransactionDirection.Outgoing
                        TransactionDirection.SelfTransfer.string -> TransactionDirection.SelfTransfer
                        TransactionDirection.Incoming.string -> TransactionDirection.Incoming
                        else -> return null
                    },
                    metadata = if (jObj["metadata"].isJsonNull) null else jObj["metadata"].asJsonObject.toString(),
                    utxoInputs = jObj["utxoInputs"]?.asJsonArray?.map {
                        TransactionInput(
                            it.asJsonObject["address"].asString,
                            it.asJsonObject["value"].asString,
                        )
                    } ?: emptyList(),
                    utxoOutputs = jObj["utxoOutputs"]?.asJsonArray?.map {
                        TransactionInput(
                            it.asJsonObject["address"].asString,
                            it.asJsonObject["value"].asString,
                        )
                    } ?: emptyList(),
                    createdAt = try {
                        format.parse(jObj["createdAt"].asString).time
                    } catch (err: Throwable) {
                        return null
                    },
                )
            }
        }
    }

    class NameRecordDeserialize : JsonDeserializer<NameRecord?> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): NameRecord? {
            val jObj = json.asJsonObject
            val name = jObj["name"].asString
            val address = jObj["address"].asString
            val chain = Chain.findByString(jObj["chain"].asString ?: return null) ?: return null
            val provider = NameProvider.entries.firstOrNull { it.string == jObj["provider"].asString } ?: NameProvider.Eths
            return NameRecord(
                name = name,
                address = address,
                chain = chain,
                provider = provider.string,
            )
        }
    }

    class ReleaseDeserialize : JsonDeserializer<Release?> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Release? {
            val jObj = json.asJsonObject
            val version = jObj["version"].asString
            val upgradeRequired = jObj["upgradeRequired"].asBoolean
            val store = jObj["store"].asString
            val platformStore = PlatformStore.entries.firstOrNull { it.string == store} ?: throw IllegalArgumentException()
            return Release(
                version = version,
                store = platformStore,
                upgradeRequired = upgradeRequired
            )
        }

    }
}

