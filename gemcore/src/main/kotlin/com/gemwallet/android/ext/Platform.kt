package com.gemwallet.android.ext

import android.os.Build
import com.wallet.core.primitives.Platform

val Platform.Companion.os: String get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY
} else {
    Build.VERSION.RELEASE
}

val Platform.Companion.model: String  get() = "${Build.MANUFACTURER} ${Build.MODEL}"