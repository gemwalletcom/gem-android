package com.gemwallet.android.data.config

import android.content.Context
import android.content.SharedPreferences
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.data.config.ConfigRepository.Companion.getGemNodeUrl
import com.gemwallet.android.services.GemApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeStatus
import uniffi.Gemstone.Config
import java.util.UUID

class OfflineFirstConfigRepository(
    private val context: Context,
) : ConfigRepository {

    private val gson = Gson()
        .newBuilder()
        .registerTypeAdapter(Node::class.java, GemApiClient.NodeSerializer())
        .create()
    private lateinit var store: SharedPreferences

    override fun authRequired(): Boolean {
        return getBoolean(Keys.Auth)
    }

    override fun setAuthRequired(enabled: Boolean) {
        putBoolean(Keys.Auth, enabled)
    }

    override fun getAppVersion(): String = getString(Keys.AppVersionProd)

    override fun setAppVersion(version: String, type: ConfigRepository.AppVersionType) {
        val key = when (type) {
            ConfigRepository.AppVersionType.Alpha -> Keys.AppVersionAlpha
            ConfigRepository.AppVersionType.Beta -> Keys.AppVersionBeta
            ConfigRepository.AppVersionType.Prod -> Keys.AppVersionProd
        }
        putString(key, version)
    }

    override fun appVersionActual(current: String): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }
        val appAppVersion = getAppVersion()
        return current.compareTo(appAppVersion) >= 0
    }

    override fun setAppVersionSkip(version: String) {
        putString(Keys.AppVersionSkip, version)
    }

    override fun getAppVersionSkip(): String {
        return getString(Keys.AppVersionSkip)
    }

    override fun getNodes(chain: Chain): List<Node> {
        val data = store.getString(Keys.Nodes.buildKey(""), null) ?: return emptyList()
        val itemType = object : TypeToken<List<ChainNodes>>() {}.type
        val chainNodes = gson.fromJson<List<ChainNodes>>(data, itemType)
        val nodes = chainNodes.firstOrNull { it.chain == chain.string }?.nodes
            ?.filter {it.status == NodeStatus.Active }
            ?.sortedBy { it.priority }
            ?: emptyList()
        return listOf(
            Node(url = getGemNodeUrl(chain), NodeStatus.Active, priority = 10)
        ) + nodes
    }

    override fun getCurrentNode(chain: Chain): Node? {
        val data = getString(Keys.CurrentNode, postfix = chain.string)
        val node = try {
            gson.fromJson(data, Node::class.java)
        } catch (err: Throwable) {
            return null
        }
        return node
    }

    override fun setCurrentNode(chain: Chain, node: Node) {
        putString(Keys.CurrentNode, gson.toJson(node), chain.string)
    }

    override fun setNodes(nodes: List<ChainNodes>) {
        putString(Keys.Nodes, gson.toJson(nodes))
    }

    override fun getBlockExplorers(chain: Chain): List<String> {
        return Config().getBlockExplorers(chain.string)
    }

    override fun getCurrentBlockExplorer(chain: Chain): String {
        return getString(Keys.CurrentExplorer, chain.string).ifEmpty {
            getBlockExplorers(chain).firstOrNull() ?: ""
        }
    }

    override fun setCurrentBlockExplorer(chain: Chain, name: String) {
        putString(Keys.CurrentExplorer, name, chain.string)
    }

    override fun getFiatAssetsVersion(): Int = getInt(Keys.FiatAssetsVersion)
    override fun setFiatAssetsVersion(version: Int) {
        putInt(Keys.FiatAssetsVersion, version)
    }

    override fun setAvailableTokenListVersion(version: Int) {
        putInt(Keys.TokenListVersion, version)
    }

    override fun setAvailableTokenListVersion(chain: String, version: Int) {
        putInt(Keys.TokenListVersion, version, chain)
    }

    override fun setOfflineListVersion(version: Int) {
        putInt(Keys.TokenListOfflineVersion, version)
    }

    override fun setOfflineTokenListVersion(chain: String, version: Int) {
        putInt(Keys.TokenListOfflineVersion, version, chain)
    }

    override fun getFiatAssets(): FiatAssets {
        val store = getStore()
        val version = getInt(Keys.FiatAssetsOfflineVersion)
        val assets = store.getStringSet(Keys.FiatAssets.string, emptySet())!!
        return FiatAssets(version.toUInt(), assets.toList())
    }

    override fun setFiatAssets(assets: FiatAssets) {
        putInt(Keys.FiatAssetsOfflineVersion, assets.version.toInt())
        getStore().edit().putStringSet(Keys.FiatAssets.string, assets.assetIds.toSet()).apply()
    }

    override fun postNotificationsGranted(granted: Boolean) {
        putBoolean(Keys.PostNotificationsGranted, granted)
    }

    override fun postNotificationsGranted(): Boolean {
        return getBoolean(Keys.PostNotificationsGranted)
    }

    override fun pushEnabled(enabled: Boolean) {
        putBoolean(Keys.PushEnabled, enabled)
    }

    override fun pushEnabled(): Boolean {
        return getBoolean(Keys.PushEnabled)
    }

    override fun updateDeviceId() {
        if (getString(Keys.DeviceId).isEmpty()) {
            putString(Keys.DeviceId, UUID.randomUUID().toString().substring(0, 31))
        }
    }

    override fun getDeviceId(): String {
        return if (BuildConfig.DEBUG) "android_debug_device_id" else getString(Keys.DeviceId)
    }

    override fun getPushToken(): String = getString(Keys.PushToken)
    override fun setPushToken(token: String) {
        putString(Keys.PushToken, token)
    }

    override fun developEnabled(enabled: Boolean) {
        putBoolean(Keys.DevelopEnabled, enabled)
    }

    override fun developEnabled(): Boolean {
        return getBoolean(Keys.DevelopEnabled)
    }

    override fun getTxSyncTime(): Long {
        return getStore().getLong(Keys.TxSyncTime.string, 0L)
    }

    override fun setTxSyncTime(time: Long) {
        getStore().edit().putLong(Keys.TxSyncTime.string, time).apply()
    }

    override fun getSubscriptionVersion(): Int {
        return getInt(Keys.SubscriptionVersion)
    }

    override fun setSubscriptionVersion(subVersion: Int) {
        putInt(Keys.SubscriptionVersion, subVersion)
    }

    override fun increaseSubscriptionVersion() {
        val newVersion = getSubscriptionVersion() + 1
        putInt(Keys.SubscriptionVersion, newVersion)
    }

    private fun getStore(): SharedPreferences {
        if (!::store.isInitialized) {
            store = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        }
        return store
    }

    private fun getInt(key: Keys, postfix: String = "") = getStore().getInt(key.buildKey(postfix), 0)

    private fun getString(key: Keys, postfix: String = "") = getStore().getString(key.buildKey(postfix), "") ?: ""

    private fun getBoolean(key: Keys, default: Boolean = false) = getStore().getBoolean(key.buildKey(), default)

    private fun putInt(key: Keys, value: Int, postfix: String = "") {
        getStore().edit().putInt(key.buildKey(postfix), value).apply()
    }

    private fun putString(key: Keys, value: String) {
        getStore().edit().putString(key.buildKey(), value).apply()
    }

    private fun putString(key: Keys, value: String, postfix: String) {
        getStore().edit().putString(key.buildKey(postfix), value).apply()
    }

    private fun putBoolean(key: Keys, value: Boolean) {
        getStore().edit().putBoolean(key.buildKey(), value).apply()
    }

    enum class Keys(val string: String) {
        Auth("auth"),
        DeviceId("device-uuid"),
        AppVersionProd("app-version-production"),
        AppVersionBeta("app-version-beta"),
        AppVersionAlpha("app-version-alpha"),
        AppVersionSkip("app-version-skip"),
        FiatAssetsVersion("fiat-assets-version"),
        FiatAssetsOfflineVersion("fiat-offline-version"),
        TokenListVersion("token-list-version"),
        TokenListOfflineVersion("token-list-offline-version"),
        FiatAssets("fiat-assets"),
        Nodes("nodes"),
        PostNotificationsGranted("post_notifications_granted"),
        PushEnabled("push_enabled"),
        PushToken("push_token"),
        DevelopEnabled("develop_enabled"),
        TxSyncTime("tx_sync_time"),
        SubscriptionVersion("subscription_version"),
        CurrentNode("current_node"),
        CurrentExplorer("current_explorer"),
        ;

        fun buildKey(postfix: String = "") = "$string-$postfix"
    }
}