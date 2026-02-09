package com.gemwallet.android.data.services.gemapi

import com.gemwallet.android.data.services.gemapi.models.PricesResponse
import com.wallet.core.primitives.AssetBasic
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarketPrice
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.Charts
import com.wallet.core.primitives.ConfigResponse
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.NameRecord
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GemApiClient {
    @GET("/v1/config")
    suspend fun getConfig(): ConfigResponse

    @POST("/v1/prices")
    suspend fun prices(@Body request: AssetPricesRequest): PricesResponse

    @GET("/v1/fiat/on_ramp/assets")
    suspend fun getOnRampAssets(): FiatAssets

    @GET("/v1/fiat/off_ramp/assets")
    suspend fun getOffRampAssets(): FiatAssets

    @GET("/v1/name/resolve/{domain}")
    suspend fun resolve(@Path("domain") domain: String, @Query("chain") chain: String): NameRecord

    @GET("/v1/charts/{asset_id}")
    suspend fun getChart(@Path("asset_id") assetId: String, @Query("period") period: String): Charts

    @GET("/v1/assets/{asset_id}")
    suspend fun getAsset(@Path("asset_id") assetId: String): AssetFull

    @GET("/v1/prices/{asset_id}")
    suspend fun getMarket(@Path("asset_id") assetId: String, @Query("currency") currency: String): AssetMarketPrice // TODO: Remove currency

    @POST("/v1/assets")
    suspend fun getAssets(@Body ids: List<AssetId>): List<AssetBasic>

    @GET("/v1/assets/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("chains") chains: String,
        @Query("tags") tags: String,
    ): List<AssetBasic>
}