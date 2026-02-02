package com.gemwallet.android.data.password

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gemwallet.android.application.SecurityStore
import com.gemwallet.android.math.decodeHex
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class TinkSecurityStore(
    @ApplicationContext private val context: Context,
) : SecurityStore<Any> {

    private val Context.dataStore by preferencesDataStore(name = "device_keys")

    init {
        AeadConfig.register();
    }

    override suspend fun getValue(key: Any): String {
        return context.dataStore.data.map { preferences -> preferences[stringPreferencesKey(key.toString())] }
            .firstOrNull()?.let {
                String(getAead().decrypt(it.decodeHex(), null))
            } ?: throw IllegalStateException("Data not found")
    }

    override suspend fun putValue(key: Any, value: String) {
        context.dataStore.edit { preferences ->
            val data = getAead().encrypt(value.toByteArray(), null)
            preferences[stringPreferencesKey(key.toString())] = data.toHexString()
        }
    }

    private fun getAead(): Aead {
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "ngen_gem_keyset", "gem_device_master_key")
            .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri("android-keystore://gem_device_master_key")
            .build()
            .keysetHandle
        return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

}