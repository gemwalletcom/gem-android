package com.gemwallet.android.data.services.gemapi

import com.gemwallet.android.data.services.gemapi.models.PricesResponse
import com.gemwallet.android.data.services.gemapi.models.Transactions
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.Charts
import com.wallet.core.primitives.ConfigResponse
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.FiatQuotes
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceData
import com.wallet.core.primitives.Subscription
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("/v1/transactions/device/{device_id}")
    suspend fun getTransactions(
        @Path("device_id") deviceId: String,
        @Query("wallet_index") walletIndex: Int,
        @Query("from_timestamp") from: Long
    ): Result<Transactions>

    @GET("/v1/name/resolve/{domain}")
    suspend fun resolve(@Path("domain") domain: String, @Query("chain") chain: String): Result<NameRecord>

    @POST("/v1/devices")
    suspend fun registerDevice(@Body request: Device): Response<Device>

    @GET("/v1/devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): Response<Device>

    @PUT("/v1/devices/{device_id}")
    suspend fun updateDevice(@Path("device_id") deviceId: String, @Body request: Device): Response<Device>

    @GET("/v1/subscriptions/{device_id}")
    suspend fun getSubscriptions(@Path("device_id") deviceId: String): Response<List<Subscription>>

    @HTTP(method = "DELETE", path = "/v1/subscriptions/{device_id}", hasBody = true)
    suspend fun deleteSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): Result<Any>

    @POST("/v1/subscriptions/{device_id}")
    suspend fun addSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): Result<Any>

    @GET("/v1/charts/{asset_id}")
    suspend fun getChart(@Path("asset_id") assetId: String, @Query("currency") currency: String, @Query("period") period: String): Result<Charts>

    @GET("/v1/assets/{asset_id}")
    suspend fun getAsset(@Path("asset_id") assetId: String, @Query("currency") currency: String): Result<AssetFull>

    @GET("/v1/prices/{asset_id}")
    suspend fun getMarket(@Path("asset_id") assetId: String, @Query("currency") currency: String): Result<PriceData>

    @POST("/v1/assets")
    suspend fun getAssets(@Body ids: List<String>): Result<List<AssetFull>>

    @GET("/v1/assets/search")
    suspend fun search(
        @Query("query") query: String,
    ): Result<List<AssetFull>>

    @GET("/v1/assets/device/{device_id}")
    suspend fun getAssets(@Path("device_id") deviceId: String, @Query("wallet_index") walletIndex: Int, @Query("from_timestamp") fromTimestamp: Int = 0): Result<List<String>>

    @POST("/v1/price_alerts/{device_id}")
    suspend fun includePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): Result<String>

    @HTTP(method = "DELETE", path = "/v1/price_alerts/{device_id}", hasBody = true)
    suspend fun excludePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): Result<String>

    @GET("/v1/price_alerts/{device_id}")
    suspend fun getPriceAlerts(@Path("device_id") deviceId: String): Result<List<PriceAlert>>
}

