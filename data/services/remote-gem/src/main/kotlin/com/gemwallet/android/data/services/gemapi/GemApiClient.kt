package com.gemwallet.android.data.services.gemapi

import com.gemwallet.android.data.services.gemapi.models.PricesResponse
import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.AssetBasic
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarketPrice
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.Charts
import com.wallet.core.primitives.ConfigResponse
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.FiatQuotes
import com.wallet.core.primitives.NFTData
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.ScanTransaction
import com.wallet.core.primitives.ScanTransactionPayload
import com.wallet.core.primitives.Subscription
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GemApiClient {
    @GET("/v1/config")
    suspend fun getConfig(): ConfigResponse

    @POST("/v1/prices")
    suspend fun prices(@Body request: AssetPricesRequest): PricesResponse

    @GET("/v1/fiat/quotes/{asset_id}")
    suspend fun getFiatQuotes(
        @Path("asset_id") assetId: String,
        @Query("type") type: String,
        @Query("fiat_amount") fiatAmount: Double?,
        @Query("crypto_value") cryptoAmount: String?,
        @Query("currency") currency: String,
        @Query("wallet_address") walletAddress: String
    ): FiatQuotes

    @GET("/v1/fiat/on_ramp/assets")
    suspend fun getOnRampAssets(): FiatAssets

    @GET("/v1/fiat/off_ramp/assets")
    suspend fun getOffRampAssets(): FiatAssets

    @GET("/v1/transactions/device/{device_id}")
    suspend fun getTransactions(
        @Path("device_id") deviceId: String,
        @Query("wallet_index") walletIndex: Int,
        @Query("from_timestamp") from: Long
    ): List<Transaction>

    @GET("/v1/name/resolve/{domain}")
    suspend fun resolve(@Path("domain") domain: String, @Query("chain") chain: String): NameRecord

    @POST("/v1/devices")
    suspend fun registerDevice(@Body request: Device): Device

    @GET("/v1/devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): Device?

    @PUT("/v1/devices/{device_id}")
    suspend fun updateDevice(@Path("device_id") deviceId: String, @Body request: Device): Device

    @GET("/v1/subscriptions/{device_id}")
    suspend fun getSubscriptions(@Path("device_id") deviceId: String): List<Subscription>?

    @HTTP(method = "DELETE", path = "/v1/subscriptions/{device_id}", hasBody = true)
    suspend fun deleteSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): String

    @POST("/v1/subscriptions/{device_id}")
    suspend fun addSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): String

    @GET("/v1/charts/{asset_id}")
    suspend fun getChart(@Path("asset_id") assetId: String, @Query("currency") currency: String, @Query("period") period: String): Charts

    @GET("/v1/assets/{asset_id}")
    suspend fun getAsset(@Path("asset_id") assetId: String, @Query("currency") currency: String): AssetFull

    @GET("/v1/prices/{asset_id}")
    suspend fun getMarket(@Path("asset_id") assetId: String, @Query("currency") currency: String): AssetMarketPrice

    @POST("/v1/assets")
    suspend fun getAssets(@Body ids: List<String>): List<AssetFull>

    @GET("/v1/assets/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("chains") chains: String,
        @Query("tags") tags: String,
    ): List<AssetBasic>

    @POST("/v1/assets")
    suspend fun search(
        @Body data: List<AssetId>,
    ): List<AssetBasic>

    @GET("/v1/assets/device/{device_id}")
    suspend fun getAssets(@Path("device_id") deviceId: String, @Query("wallet_index") walletIndex: Int, @Query("from_timestamp") fromTimestamp: Int = 0): List<String>

    @POST("/v1/price_alerts/{device_id}")
    suspend fun includePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): String

    @HTTP(method = "DELETE", path = "/v1/price_alerts/{device_id}", hasBody = true)
    suspend fun excludePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): String

    @GET("/v1/price_alerts/{device_id}")
    suspend fun getPriceAlerts(@Path("device_id") deviceId: String): List<PriceAlert>

    @GET("/v1/nft/assets/device/{device_id}")
    suspend fun getNFTs(@Path("device_id") deviceId: String, @Query("wallet_index") walletIndex: Int): Data<List<NFTData>>

    @POST("/v1/scan/transaction")
    suspend fun getScanTransaction(@Body payload: ScanTransactionPayload): Data<ScanTransaction>
}

@Serializable
data class Data<T>(
    val data: T
)