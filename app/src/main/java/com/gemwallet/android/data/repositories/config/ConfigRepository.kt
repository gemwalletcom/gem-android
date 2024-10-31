package com.gemwallet.android.data.repositories.config

import com.wallet.core.primitives.FiatAssets

interface ConfigRepository {
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
}