package com.gemwallet.android.data.services.gemapi

import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.AuthNonce
import com.wallet.core.primitives.AuthenticatedRequest
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.FiatQuoteUrl
import com.wallet.core.primitives.FiatQuotes
import com.wallet.core.primitives.MigrateDeviceIdRequest
import com.wallet.core.primitives.NFTData
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.RedemptionRequest
import com.wallet.core.primitives.RedemptionResult
import com.wallet.core.primitives.ReferralCode
import com.wallet.core.primitives.RewardEvent
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.ScanTransaction
import com.wallet.core.primitives.ScanTransactionPayload
import com.wallet.core.primitives.SupportDevice
import com.wallet.core.primitives.SupportDeviceRequest
import com.wallet.core.primitives.WalletSubscription
import com.wallet.core.primitives.WalletSubscriptionChains
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

const val WALLET_ID_HEADER = "x-wallet-id"

interface GemDeviceApiClient {

    // Device manage
    @GET("/v2/devices")
    suspend fun getDevice(): Device?

    @POST("/v2/devices")
    suspend fun registerDevice(@Body request: Device): Device

    @POST("/v2/devices/migrate")
    suspend fun migrateDevice(@Body request: MigrateDeviceIdRequest): Device

    @PUT("/v2/devices")
    suspend fun updateDevice(@Body request: Device): Device

    @GET("/v2/devices/is_registered")
    suspend fun isDeviceRegistered(): Boolean

    // Subscriptions
    @GET("/v2/devices/subscriptions")
    suspend fun getSubscriptions(): List<WalletSubscriptionChains>?

    @POST("/v2/devices/subscriptions")
    suspend fun addSubscriptions(@Body request: List<WalletSubscription>): Int

    @HTTP(method = "DELETE", path = "/v2/devices/subscriptions", hasBody = true)
    suspend fun deleteSubscriptions(@Body request: List<WalletSubscriptionChains>): Int

    // Price Alerts
    @GET("/v2/devices/price_alerts")
    suspend fun getPriceAlerts(): List<PriceAlert>

    @POST("/v2/devices/price_alerts")
    suspend fun includePriceAlert(@Body alerts: List<PriceAlert>): String

    @HTTP(method = "DELETE", path = "/v2/devices/price_alerts", hasBody = true)
    suspend fun excludePriceAlert(@Body assets: List<PriceAlert>): String

    // Rewards
    @GET("/v2/devices/rewards")
    suspend fun getRewards(@Header(WALLET_ID_HEADER) walletId: String): Rewards

    @GET("/v2/devices/rewards/events")
    suspend fun getRewardsEvents(@Header(WALLET_ID_HEADER)  walletId: String): List<RewardEvent>

    @GET("/v2/devices/rewards/redemptions/{code}")
    suspend fun getRedemptionOption(@Path("code") code: String): RewardRedemptionOption

//    @GET("/v1/devices/{device_id}/rewards/leaderboard")
//    suspend fun getRewardsLeaderboard(@Path("device_id") deviceId: String): ReferralLeaderboard

    @POST("/v2/devices/rewards/referrals/create")
    suspend fun createReferral(@Header(WALLET_ID_HEADER)  walletId: String, @Body body: AuthenticatedRequest<ReferralCode>): Rewards

    @POST("/v2/devices/rewards/referrals/use")
    suspend fun useReferralCode(@Header(WALLET_ID_HEADER)  walletId: String, @Body body: AuthenticatedRequest<ReferralCode>): List<RewardEvent>

    @POST("/v2/devices/rewards/redeem")
    suspend fun redeem(@Header(WALLET_ID_HEADER)  walletId: String, @Body request: AuthenticatedRequest<RedemptionRequest>): RedemptionResult

    // Transactions
    @GET("/v2/devices/transactions?from_timestamp={from_timestamp}")
    suspend fun getTransactions(
        @Header(WALLET_ID_HEADER)  walletId: String,
        @Query("from_timestamp") from: Long,
    ): List<Transaction>

    @GET("/v2/devices/transactions?from_timestamp={from_timestamp}&asset_id")
    suspend fun getTransactions(
        @Header(WALLET_ID_HEADER)  walletId: String,
        @Query("asset_id") assetId: String,
        @Query("from_timestamp") from: Long,
    ): List<Transaction>

    @POST("/v2/devices/scan/transaction")
    suspend fun getScanTransaction(@Body payload: ScanTransactionPayload): ScanTransaction

    // Assets
    @GET("/v2/devices/assets?from_timestamp={fromTimestamp}")
    suspend fun getAssets(@Header(WALLET_ID_HEADER)  walletId: String, @Query("from_timestamp") fromTimestamp: Int = 0): List<String>

    // NFT
    @GET("/v2/devices/nft_assets")
    suspend fun getNFTs(@Header(WALLET_ID_HEADER)  walletId: String): List<NFTData>

    // AUTH
    @GET("/v2/devices/auth/nonce")
    suspend fun getAuthNonce(): AuthNonce

    // Support
    @POST("/v2/devices/support")
    suspend fun registerSupport(@Body request: SupportDeviceRequest): SupportDevice

    // BUY
    @GET("/v2/devices/fiat/quotes/{type}/{asset_id}")
    suspend fun getFiatQuotes(
        @Header(WALLET_ID_HEADER)  walletId: String,
        @Path("type") type: String,
        @Path("asset_id") assetId: String,
        @Query("amount") amount: Double,
        @Query("currency") currency: String,
    ): FiatQuotes

    @GET("/v2/devices/fiat/quotes/{quote_id}/url")
    suspend fun getFiatQuoteUrl(
        @Header(WALLET_ID_HEADER) walletId: String,
        @Path("quote_id") quoteId: String
    ): FiatQuoteUrl

}