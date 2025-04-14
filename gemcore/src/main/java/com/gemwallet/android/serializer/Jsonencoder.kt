package com.gemwallet.android.serializer

import kotlinx.serialization.json.Json

val jsonEncoder by lazy {
    Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
}