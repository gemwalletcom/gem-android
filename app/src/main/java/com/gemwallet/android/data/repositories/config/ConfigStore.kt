package com.gemwallet.android.data.repositories.config

import android.content.SharedPreferences

class ConfigStore(
    var store: SharedPreferences,
) {
    fun getInt(key: String, postfix: String = "") = store.getInt(buildKey(key, postfix), 0)

    fun getString(key: String, postfix: String = "") = store.getString(buildKey(key, postfix), "") ?: ""

    fun getBoolean(key: String, default: Boolean = false) = store.getBoolean(buildKey(key), default)

    fun putInt(key: String, value: Int, postfix: String = "") {
        store.edit().putInt(buildKey(key, postfix), value).apply()
    }

    fun putString(key: String, value: String, postfix: String = "") {
        store.edit().putString(buildKey(key, postfix), value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        store.edit().putBoolean(buildKey(key), value).apply()
    }

    private fun buildKey(key: String, postfix: String = "") = "$key-$postfix"
}