package com.gemwallet.android.blockchain

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

enum class Mime(val value: MediaType) {
    Plain("text/plain; charset=utf-8".toMediaType()),
    Json("application/json; charset=utf-8".toMediaType())
}