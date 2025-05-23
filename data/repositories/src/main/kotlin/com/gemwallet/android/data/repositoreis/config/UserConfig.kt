package com.gemwallet.android.data.repositoreis.config

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gemwallet.android.cases.config.GetLatestVersion
import com.gemwallet.android.cases.config.GetLockInterval
import com.gemwallet.android.cases.config.SetLatestVersion
import com.gemwallet.android.cases.config.SetLockInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserConfig(
    private val context: Context,
) : GetLatestVersion,
        SetLatestVersion,
        GetLockInterval,
        SetLockInterval
{

    private lateinit var store: SharedPreferences
    private val Context.dataStore by preferencesDataStore(name = "user_config")

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

    fun isHideBalances(): Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Key.IsHideBalances] == true }

    suspend fun hideBalances() {
        context.dataStore.edit { preferences ->
            preferences[Key.IsHideBalances] = preferences[Key.IsHideBalances] != true
        }
    }

    override fun getLatestVersion(): Flow<String> = context.dataStore.data
        .map { preferences -> preferences[Key.LatestVersion] ?: "" }

    override suspend fun setLatestVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[Key.LatestVersion] = version
        }
    }

    override fun getLockInterval(): Flow<Int> = context.dataStore.data
    .map { preferences -> preferences[Key.LockInterval] ?: 1 }

    override suspend fun setLockInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[Key.LockInterval] = minutes
        }
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

    private object Key {
        val IsHideBalances = booleanPreferencesKey("hide_balances")
        val LatestVersion = stringPreferencesKey("latest_version")
        val LockInterval = intPreferencesKey("lock_interval")
    }
}