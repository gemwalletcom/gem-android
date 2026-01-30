package com.gemwallet.android.data.password

import android.content.Context
import com.gemwallet.android.application.PasswordStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import java.security.SecureRandom


class TinkPasswordStore(
    @ApplicationContext private val context: Context
) : PasswordStore {

    val random = SecureRandom()

    override fun createPassword(key: String): String {
        TODO("Not yet implemented")
    }

    override fun removePassword(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPassword(key: String): String {
        TODO("Not yet implemented")
    }

    override fun putPassword(key: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun getDevicePrivateKey(): ByteArray {
        TODO("Not yet implemented")
    }

    @Throws(GeneralSecurityException::class)
    private fun store(): Aead {
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "ngen_gem_keyset", "master_key")
            .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri("android-keystore://master_key")
            .build()
            .keysetHandle
        return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

}