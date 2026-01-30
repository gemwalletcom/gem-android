package com.gemwallet.android.data.password

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.application.PasswordStore.Keys
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import wallet.core.jni.Curve
import wallet.core.jni.HDWallet
import java.security.SecureRandom

class PreferencePasswordStore(
    private val context: Context,
) : PasswordStore {
    val random = SecureRandom()

    @SuppressLint("ApplySharedPref")
    override fun createPassword(walletId: String): String {
        val key = ByteArray(32)
        random.nextBytes(key)
        getStore().edit(commit = true) {
            putString(walletId, key.toHexString())
        }
        return key.toHexString()
    }

    override fun removePassword(key: String): Boolean =
        getStore().edit().remove(key).commit()

    override fun getPassword(key: String): String {
        val password = getStore().getString(key, null)
            ?: throw IllegalAccessError("Password doesn't found")

        return password
    }

    override fun putPassword(key: String, password: String) {
        getStore().edit(commit = true) {
            putString(key, password)
        }
    }

    override fun getDevicePrivateKey(): ByteArray {
        try {
            val data = getPassword(Keys.DevicePrivateKey.key)
            return data.decodeHex()
        } catch (_: Throwable) {}

        val deviceKey = HDWallet(128, "").getMasterKey(Curve.ED25519)
        putPassword(Keys.DevicePrivateKey.key, deviceKey.data().toHexString())
        putPassword(Keys.DevicePublicKey.key, deviceKey.publicKeyEd25519.data().toHexString())
        return deviceKey.data()
    }

    private fun getStore(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//                .setUserAuthenticationRequired(true, 1)
//            .setRequestStrongBoxBacked(true)
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