package com.gemwallet.android.data.repositories.config

import android.content.Context
import android.content.SharedPreferences
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.serializer.AssetIdSerializer
import com.gemwallet.android.services.GemApiClient
import com.google.gson.Gson
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatAssets
import com.wallet.core.primitives.Node
import uniffi.Gemstone.Config
import java.util.UUID

class OfflineFirstConfigRepository(
    private val context: Context,
) : ConfigRepository {

    private val gson = Gson()
        .newBuilder()
        .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
        .registerTypeAdapter(Node::class.java, GemApiClient.NodeSerializer())
        .create()
    private lateinit var store: SharedPreferences

    override fun authRequired(): Boolean {
        return getBoolean(Keys.Auth)
    }

    override fun setAuthRequired(enabled: Boolean) {
        putBoolean(Keys.Auth, enabled)
    }

    override fun setAppVersionSkip(version: String) {
        putString(Keys.AppVersionSkip, version)
    }

    override fun getAppVersionSkip(): String {
        return getString(Keys.AppVersionSkip)
    }

    override fun getCurrentNode(chain: Chain): Node? {
        val data = getString(Keys.CurrentNode, postfix = chain.string)
        val node = try {
            gson.fromJson(data, Node::class.java)
        } catch (_: Throwable) {
            return null
        }
        return node
    }

    override fun setCurrentNode(chain: Chain, node: Node) {
        putString(Keys.CurrentNode, gson.toJson(node), chain.string)
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

    override fun getLaunchNumber(): Int {
        return getInt(Keys.LaunchNumber)
    }

    override fun increaseLaunchNumber() {
        putInt(Keys.LaunchNumber, getInt(Keys.LaunchNumber) + 1)
    }

    override fun setEnablePriceAlerts(enabled: Boolean) {
        putBoolean(Keys.PriceAlertsEnabled, enabled)
    }

    override fun isPriceAlertEnabled(): Boolean {
        return getBoolean(Keys.PriceAlertsEnabled)
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
        AppVersionSkip("app-version-skip"),
        FiatAssetsVersion("fiat-assets-version"),
        FiatAssetsOfflineVersion("fiat-offline-version"),
        TokenListVersion("token-list-version"),
        TokenListOfflineVersion("token-list-offline-version"),
        FiatAssets("fiat-assets"),
        PostNotificationsGranted("post_notifications_granted"),
        PushEnabled("push_enabled"),
        PushToken("push_token"),
        DevelopEnabled("develop_enabled"),
        TxSyncTime("tx_sync_time"),
        SubscriptionVersion("subscription_version"),
        CurrentNode("current_node"),
        CurrentExplorer("current_explorer"),
        LaunchNumber("launch_number"),
        PriceAlertsEnabled("price_alerts_enabled"),
        ;

        fun buildKey(postfix: String = "") = "$string-$postfix"
    }
}