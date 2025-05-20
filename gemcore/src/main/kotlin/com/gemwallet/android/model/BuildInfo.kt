package com.gemwallet.android.model

import com.gemwallet.android.cases.device.RequestPushToken
import com.wallet.core.primitives.PlatformStore

data class BuildInfo(
    val platformStore: PlatformStore,
    val versionName: String,
    val requestPushToken: RequestPushToken
)
