package com.gemwallet.android.data.services.gemapi

import com.gemwallet.android.data.services.gemapi.models.PricesResponse
import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.AssetBasic
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarketPrice
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.AuthNonce
import com.wallet.core.primitives.AuthenticatedRequest
import com.wallet.core.primitives.Charts
import com.wallet.core.primitives.ConfigResponse
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.FiatQuoteUrl
import com.wallet.core.primitives.FiatQuoteUrlRequest
import com.wallet.core.primitives.FiatQuotes
import com.wallet.core.primitives.NFTData
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.RedemptionRequest
import com.wallet.core.primitives.RedemptionResult
import com.wallet.core.primitives.ReferralCode
import com.wallet.core.primitives.ReferralLeaderboard
import com.wallet.core.primitives.RewardEvent
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.ScanTransaction
import com.wallet.core.primitives.ScanTransactionPayload
import com.wallet.core.primitives.Subscription
import com.wallet.core.primitives.SupportDevice
import com.wallet.core.primitives.SupportDeviceRequest
import com.wallet.core.primitives.WalletSubscription
import com.wallet.core.primitives.WalletSubscriptionChains
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

    @GET("/v1/fiat/quotes/sell/{asset_id}")
    suspend fun getBuyFiatQuotes(
        @Path("asset_id") assetId: String,
        @Query("amount") amount: Double,
        @Query("device_id") deviceId: String,
        @Query("currency") currency: String,
    ): FiatQuotes

    @GET("/v1/fiat/quotes/sell/{asset_id}")
    suspend fun getSellFiatQuotes(
        @Path("asset_id") assetId: String,
        @Query("amount") amount: Double,
        @Query("currency") currency: String,
        @Query("device_id") deviceId: String,
    ): FiatQuotes

    @POST("/v1/fiat/quotes/url")
    suspend fun getFiatQuoteUrl(@Body request: FiatQuoteUrlRequest): FiatQuoteUrl

    @GET("/v1/fiat/on_ramp/assets")
    suspend fun getOnRampAssets(): FiatAssets

    @GET("/v1/fiat/off_ramp/assets")
    suspend fun getOffRampAssets(): FiatAssets

    @GET("/v1/devices/{device_id}/wallets/{wallet_id}/transactions")
    suspend fun getTransactions(
        @Path("device_id") deviceId: String,
        @Path("wallet_id") walletId: String,
        @Query("from_timestamp") from: Long
    ): List<Transaction>

    @GET("/v1/name/resolve/{domain}")
    suspend fun resolve(@Path("domain") domain: String, @Query("chain") chain: String): NameRecord

    @POST("/v1/devices")
    suspend fun registerDevice(@Body request: Device): Device

    @GET("/v1/devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): Device?

    @GET("/v1/devices/{device_id}/is_registered")
    suspend fun isDeviceRegistered(@Path("device_id") deviceId: String): Boolean

    @POST("/v1/devices/{device_id}/support")
    suspend fun registerSupport(@Path("device_id") deviceId: String, @Body request: SupportDeviceRequest): SupportDevice

    @PUT("/v1/devices/{device_id}")
    suspend fun updateDevice(@Path("device_id") deviceId: String, @Body request: Device): Device

    @GET("/v1/subscriptions/{device_id}")
    suspend fun getOldSubscriptions(@Path("device_id") deviceId: String): List<Subscription>?

    @HTTP(method = "DELETE", path = "/v1/subscriptions/{device_id}", hasBody = true)
    suspend fun deleteOldSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): Int

    @POST("/v1/subscriptions/{device_id}")
    suspend fun addOldSubscriptions(@Path("device_id") deviceId: String, @Body request: List<Subscription>): Int

    @GET("/v1/devices/{device_id}/subscriptions")
    suspend fun getSubscriptions(@Path("device_id") deviceId: String): List<WalletSubscriptionChains>?

    @HTTP(method = "DELETE", path = "/v1/devices/{device_id}/subscriptions", hasBody = true)
    suspend fun deleteSubscriptions(@Path("device_id") deviceId: String, @Body request: List<WalletSubscription>): Int

    @POST("/v1/devices/{device_id}/subscriptions")
    suspend fun addSubscriptions(@Path("device_id") deviceId: String, @Body request: List<WalletSubscription>): Int

    @GET("/v1/charts/{asset_id}")
    suspend fun getChart(@Path("asset_id") assetId: String, @Query("currency") currency: String, @Query("period") period: String): Charts

    @GET("/v1/assets/{asset_id}")
    suspend fun getAsset(@Path("asset_id") assetId: String, @Query("currency") currency: String): AssetFull

    @GET("/v1/prices/{asset_id}")
    suspend fun getMarket(@Path("asset_id") assetId: String, @Query("currency") currency: String): AssetMarketPrice

    @POST("/v1/assets")
    suspend fun getAssets(@Body ids: List<AssetId>): List<AssetBasic>

    @GET("/v1/assets/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("chains") chains: String,
        @Query("tags") tags: String,
    ): List<AssetBasic>

    @GET("/v1/devices/{device_id}/wallets/{wallet_id}/assets")
    suspend fun getAssets(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String, @Query("from_timestamp") fromTimestamp: Int = 0): List<String>

    @POST("/v1/devices/{device_id}/price_alerts")
    suspend fun includePriceAlert(@Path("device_id") deviceId: String, @Body alerts: List<PriceAlert>): String

    @HTTP(method = "DELETE", path = "/v1/devices/{device_id}/price_alerts", hasBody = true)
    suspend fun excludePriceAlert(@Path("device_id") deviceId: String, @Body assets: List<PriceAlert>): String

    @GET("/v1/devices/{device_id}/price_alerts")
    suspend fun getPriceAlerts(@Path("device_id") deviceId: String): List<PriceAlert>

    @GET("/v1/devices/{device_id}/wallets/{wallet_id}/nft_assets")
    suspend fun getNFTs(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String): List<NFTData>

    @POST("/v2/scan/transaction")
    suspend fun getScanTransaction(@Body payload: ScanTransactionPayload): ScanTransaction

    @GET("/v1/devices/{device_id}/auth/nonce")
    suspend fun getAuthNonce(@Path("device_id")deviceId: String): AuthNonce

    @GET("/v1/devices/{device_id}/wallets/{wallet_id}/rewards")
    suspend fun getRewards(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String): Rewards

    @GET("/v1/devices/{device_id}/wallets/{wallet_id}/rewards/events")
    suspend fun getRewardsEvents(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String): List<RewardEvent>

    @GET("/v1/devices/{device_id}/rewards/leaderboard")
    suspend fun getRewardsLeaderboard(@Path("device_id") deviceId: String): ReferralLeaderboard

    @GET("/v1/devices/{device_id}/rewards/redemptions/{code}")
    suspend fun getRedemptionOption(@Path("device_id") deviceId: String, @Path("code") code: String): RewardRedemptionOption

    @POST("/v1/devices/{device_id}/wallets/{wallet_id}/rewards/referrals/create")
    suspend fun createReferral(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String, @Body body: AuthenticatedRequest<ReferralCode>): Rewards

    @POST("/v1/devices/{device_id}/wallets/{wallet_id}/rewards/referrals/use")
    suspend fun useReferralCode(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String, @Body body: AuthenticatedRequest<ReferralCode>): List<RewardEvent>

    @POST("/v1/devices/{device_id}/wallets/{wallet_id}/rewards/redeem")
    suspend fun redeem(@Path("device_id") deviceId: String, @Path("wallet_id") walletId: String, @Body request: AuthenticatedRequest<RedemptionRequest>): RedemptionResult
}