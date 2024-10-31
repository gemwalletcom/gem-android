package com.gemwallet.android.data.repositories.config

import com.wallet.core.primitives.FiatAssets

interface ConfigRepository {
    // Buy
    fun getFiatAssetsVersion(): Int
    fun setFiatAssetsVersion(version: Int)
    fun getFiatAssets(): FiatAssets
    fun setFiatAssets(assets: FiatAssets)

    // Pushes
    fun postNotificationsGranted(granted: Boolean)
    fun postNotificationsGranted(): Boolean
    fun pushEnabled(enabled: Boolean)
    fun pushEnabled(): Boolean
    fun getPushToken(): String
    fun setPushToken(token: String)
    fun getSubscriptionVersion(): Int
    fun setSubscriptionVersion(subVersion: Int)
    fun increaseSubscriptionVersion()

    fun updateDeviceId()
    fun getDeviceId(): String

    // UserConfig
    fun developEnabled(enabled: Boolean)
    fun developEnabled(): Boolean
    fun getLaunchNumber(): Int
    fun increaseLaunchNumber()
    fun authRequired(): Boolean
    fun setAuthRequired(enabled: Boolean)
    fun getAppVersionSkip(): String
    fun setAppVersionSkip(version: String)

    // Price - out to PriceAlertsRepository
    fun setEnablePriceAlerts(enabled: Boolean)
    fun isPriceAlertEnabled(): Boolean


    // TODO: To remove
//    companion object {
//        fun getGemNodeUrl(chain: Chain) = "https://${chain.string}.gemnodes.com"
//
//        fun getGemNode(chain: Chain) = Node(
//            url = getGemNodeUrl(chain),
//            NodeState.Active,
//            priority = 10
//        )
//    }
}