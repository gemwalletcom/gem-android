package com.gemwallet.android.data.password

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.math.toHexString
import java.security.SecureRandom
import androidx.core.content.edit

class PreferencePasswordStore(
    private val context: Context,
) : PasswordStore {
    val random = SecureRandom()

    @SuppressLint("ApplySharedPref")
    override fun createPassword(walletId: String): String {
        val key = ByteArray(16)
        random.nextBytes(key)
        getStore().edit(commit = true) {
            putString(walletId, key.toHexString())
        }
        return key.toHexString()
    }

    override fun removePassword(walletId: String): Boolean =
        getStore().edit().remove(walletId).commit()

    override fun getPassword(walletId: String): String =
        getStore().getString(walletId, null) ?: throw IllegalAccessError("Password doesn't found")

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