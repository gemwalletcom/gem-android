package com.gemwallet.android.data.services.gemapi.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.primitives.PlatformStore
import com.wallet.core.primitives.Release
import java.lang.reflect.Type

class ReleaseDeserialize : JsonDeserializer<Release?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Release? {
        val jObj = json.asJsonObject
        val version = jObj["version"].asString
        val upgradeRequired = jObj["upgradeRequired"].asBoolean
        val store = jObj["store"].asString
        val platformStore = PlatformStore.entries.firstOrNull { it.string == store} ?: throw IllegalArgumentException()
        return Release(
            version = version,
            store = platformStore,
            upgradeRequired = upgradeRequired
        )
    }

}