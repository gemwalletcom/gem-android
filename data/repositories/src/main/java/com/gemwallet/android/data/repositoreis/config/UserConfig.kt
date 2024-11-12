package com.gemwallet.android.data.repositoreis.config

import android.content.Context
import android.content.SharedPreferences

class UserConfig(
    private val context: Context,
) {
    private lateinit var store: SharedPreferences

    fun authRequired(): Boolean {
        return getBoolean(Keys.Auth)
    }

    fun setAuthRequired(enabled: Boolean) {
        putBoolean(Keys.Auth, enabled)
    }

    fun setAppVersionSkip(version: String) {
        putString(Keys.AppVersionSkip, version)
    }

    fun getAppVersionSkip(): String {
        return getString(Keys.AppVersionSkip)
    }

    fun developEnabled(enabled: Boolean) {
        putBoolean(Keys.DevelopEnabled, enabled)
    }

    fun developEnabled(): Boolean {
        return getBoolean(Keys.DevelopEnabled)
    }

    fun getLaunchNumber(): Int {
        return getInt(Keys.LaunchNumber)
    }

    fun increaseLaunchNumber() {
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
        DevelopEnabled("develop_enabled"),
        SubscriptionVersion("subscription_version"),
        LaunchNumber("launch_number"),
        ;

        fun buildKey(postfix: String = "") = "$string-$postfix"
    }
}