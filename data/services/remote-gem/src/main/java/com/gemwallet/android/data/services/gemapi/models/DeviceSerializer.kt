package com.gemwallet.android.data.services.gemapi.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.Platform
import com.wallet.core.primitives.PlatformStore
import java.lang.reflect.Type

class DeviceSerializer : JsonDeserializer<Device>, JsonSerializer<Device> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Device {
        val jObj = json?.asJsonObject ?: throw JsonSyntaxException(json?.toString())
        val platformStore = if (jObj["platformStore"].isJsonNull) null else jObj["platformStore"]?.asString
        return Device(
            id = jObj["id"]?.asString ?: "",
            platform = when (jObj["platform"]?.asString) {
                Platform.IOS.string -> Platform.IOS
                else -> Platform.Android
            },
            platformStore = PlatformStore.entries.firstOrNull { it.string == platformStore },
            isPriceAlertsEnabled = jObj["isPriceAlertsEnabled"]?.asBoolean == true,
            token = jObj["token"]?.asString ?: "",
            locale = jObj["locale"]?.asString ?: "",
            version = jObj["version"]?.asString ?: "",
            isPushEnabled = jObj["isPushEnabled"]?.asBoolean ?: false,
            currency = jObj["currency"]?.asString ?: Currency.USD.string,
            subscriptionsVersion = jObj["subscriptionsVersion"]?.asInt ?: 0,
        )
    }

    override fun serialize(
        src: Device,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonObject().apply {
            addProperty("id", src.id)
            addProperty("platform", src.platform.string)
            addProperty("platformStore", src.platformStore?.string)
            addProperty("token", src.token)
            addProperty("locale", src.locale)
            addProperty("version", src.version)
            addProperty("isPushEnabled", src.isPushEnabled)
            addProperty("currency", src.currency)
            addProperty("subscriptionsVersion", src.subscriptionsVersion)
            addProperty("isPriceAlertsEnabled", src.isPriceAlertsEnabled)
        }
    }

}