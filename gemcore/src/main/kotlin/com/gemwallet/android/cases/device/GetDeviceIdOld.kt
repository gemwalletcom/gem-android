package com.gemwallet.android.cases.device

interface GetDeviceIdOld {
    fun getDeviceId(): String

    fun isMigrated(): Boolean

    fun migrated()
}