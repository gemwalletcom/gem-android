package com.gemwallet.android.data.services.gemapi.models

import com.gemwallet.android.ext.findByString
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameProvider
import com.wallet.core.primitives.NameRecord
import java.lang.reflect.Type

class NameRecordDeserialize : JsonDeserializer<NameRecord?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NameRecord? {
        val jObj = json.asJsonObject
        val name = jObj["name"].asString
        val address = jObj["address"].asString
        val chain = Chain.findByString(jObj["chain"].asString ?: return null) ?: return null
        val provider = NameProvider.entries.firstOrNull { it.string == jObj["provider"].asString } ?: NameProvider.Eths
        return NameRecord(
            name = name,
            address = address,
            chain = chain,
            provider = provider.string,
        )
    }
}