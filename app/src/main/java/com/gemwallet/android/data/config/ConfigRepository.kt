package com.gemwallet.android.data.config

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import com.wallet.core.primitives.PriceAlert

interface ConfigRepository {

    fun authRequired(): Boolean

    fun setAuthRequired(enabled: Boolean)

    fun getAppVersionSkip(): String

    fun setAppVersionSkip(version: String)

    fun getCurrentNode(chain: Chain): Node?

    fun setCurrentNode(chain: Chain, node: Node)

    fun getBlockExplorers(chain: Chain): List<String>

    fun getCurrentBlockExplorer(chain: Chain): String

    fun setCurrentBlockExplorer(chain: Chain, name: String)

    fun getFiatAssetsVersion(): Int

    fun setFiatAssetsVersion(version: Int)

    fun setAvailableTokenListVersion(version: Int)

    fun setAvailableTokenListVersion(chain: String, version: Int)

    fun setOfflineListVersion(version: Int)

    fun setOfflineTokenListVersion(chain: String, version: Int)

    fun getFiatAssets(): FiatAssets

    fun setFiatAssets(assets: FiatAssets)

    fun postNotificationsGranted(granted: Boolean)

    fun postNotificationsGranted(): Boolean

    fun pushEnabled(enabled: Boolean)

    fun pushEnabled(): Boolean

    fun updateDeviceId()

    fun getDeviceId(): String

    fun getPushToken(): String

    fun setPushToken(token: String)

    fun developEnabled(enabled: Boolean)

    fun developEnabled(): Boolean

    fun getTxSyncTime(): Long

    fun setTxSyncTime(time: Long)

    fun getSubscriptionVersion(): Int

    fun setSubscriptionVersion(subVersion: Int)

    fun increaseSubscriptionVersion()

    fun getLaunchNumber(): Int

    fun increaseLaunchNumber()

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