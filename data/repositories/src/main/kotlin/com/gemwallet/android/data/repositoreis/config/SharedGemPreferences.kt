package com.gemwallet.android.data.repositoreis.config

import android.content.SharedPreferences
import androidx.core.content.edit
import uniffi.gemstone.GemPreferences

class SharedGemPreferences(
    private val sharedPreferences: SharedPreferences
) : GemPreferences {

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun set(key: String, value: String) {
        sharedPreferences.edit(commit = true) { putString(key, value) }
    }

    override fun remove(key: String) {
        sharedPreferences.edit(commit = true) { remove(key) }
    }
}