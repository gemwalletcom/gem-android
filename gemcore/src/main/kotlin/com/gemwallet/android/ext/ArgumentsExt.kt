package com.gemwallet.android.ext

import java.net.URLDecoder
import java.net.URLEncoder

fun String.urlDecode(): String {
    return if (isNullOrEmpty()) {
        ""
    } else {
        URLDecoder.decode(this, Charsets.UTF_8.name())
    }
}

fun String.urlEncode(): String? {
    return if (isNullOrEmpty()) {
        null
    } else {
        URLEncoder.encode(this, Charsets.UTF_8.name())
    }
}