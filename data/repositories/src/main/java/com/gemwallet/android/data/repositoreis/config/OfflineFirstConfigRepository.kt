package com.gemwallet.android.data.repositoreis.config

import android.content.Context
import android.content.SharedPreferences
import com.gemwallet.android.data.repositoreis.BuildConfig
import java.util.UUID

class OfflineFirstConfigRepository(
    private val context: Context,
) : ConfigRepository {
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
        PostNotificationsGranted("post_notifications_granted"),
        PushEnabled("push_enabled"),
        PushToken("push_token"),
        DevelopEnabled("develop_enabled"),
        SubscriptionVersion("subscription_version"),
        LaunchNumber("launch_number"),
        ;

        fun buildKey(postfix: String = "") = "$string-$postfix"
    }
}