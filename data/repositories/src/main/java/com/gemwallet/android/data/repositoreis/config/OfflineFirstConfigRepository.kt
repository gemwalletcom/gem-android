package com.gemwallet.android.data.repositoreis.config

import android.content.Context
import android.content.SharedPreferences

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

    override fun developEnabled(enabled: Boolean) {
        putBoolean(Keys.DevelopEnabled, enabled)
    }

    override fun developEnabled(): Boolean {
        return getBoolean(Keys.DevelopEnabled)
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

    private fun putBoolean(key: Keys, value: Boolean) {
        getStore().edit().putBoolean(key.buildKey(), value).apply()
    }

    enum class Keys(val string: String) {
        Auth("auth"),
        DeviceId("device-uuid"),
        AppVersionSkip("app-version-skip"),
//        PushEnabled("push_enabled"),
//        PushToken("push_token"),
        DevelopEnabled("develop_enabled"),
        SubscriptionVersion("subscription_version"),
        LaunchNumber("launch_number"),
        ;

        fun buildKey(postfix: String = "") = "$string-$postfix"
    }
}