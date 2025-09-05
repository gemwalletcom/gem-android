package com.gemwallet.android.data.repositoreis.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import uniffi.gemstone.GemPreferences

class SecurityGemPreferences(
    private val context: Context,
) : GemPreferences {

    override fun get(key: String): String? {
        return getStore().getString(key, null)
    }

    override fun set(key: String, value: String) {
        getStore().edit(commit = true) {
            putString(key, value)
        }
    }

    override fun remove(key: String) {
        getStore().edit(commit = true) {
            remove(key)
        }
    }

    private fun getStore(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "pwd",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}