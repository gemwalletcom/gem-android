package com.gemwallet.android.ext

import java.net.URI

fun String.getShortUrl(): String? {
    try {
        val uri = URI(this)
        val host: String? = uri.host
        return host?.removePrefix("www.")
    } catch (e: Exception) {
        return null
    }
}