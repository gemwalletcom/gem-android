package com.gemwallet.android.data.repositories.config

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState

interface ConfigRepository {

    // Nodes - out to NodesRepository
    fun getCurrentNode(chain: Chain): Node?
    fun setCurrentNode(chain: Chain, node: Node)
    fun getBlockExplorers(chain: Chain): List<String>
    fun getCurrentBlockExplorer(chain: Chain): String
    fun setCurrentBlockExplorer(chain: Chain, name: String)

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

    companion object {
        fun getGemNodeUrl(chain: Chain) = "https://${chain.string}.gemnodes.com"

        fun getGemNode(chain: Chain) = Node(
            url = getGemNodeUrl(chain),
            NodeState.Active,
            priority = 10
        )
    }
}