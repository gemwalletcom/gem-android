package com.gemwallet.android.ui

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri

fun UriHandler.open(context: Context, uri: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, uri.toUri())
    } catch (err: Throwable) {
        try {
            openUri(uri)
        } catch (_: Throwable) {
        }
    }
}