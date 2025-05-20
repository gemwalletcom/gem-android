package com.gemwallet.android.ui.components

import android.util.Log
import androidx.compose.ui.platform.UriHandler

fun UriHandler.open(uri: String) {
    try {
        openUri(uri)
    } catch (err: Throwable) {
        Log.d("OPEN_URI", "Open uri error", err)
    }
}