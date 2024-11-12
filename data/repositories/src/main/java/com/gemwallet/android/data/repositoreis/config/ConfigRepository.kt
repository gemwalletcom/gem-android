package com.gemwallet.android.data.repositoreis.config

interface ConfigRepository {
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