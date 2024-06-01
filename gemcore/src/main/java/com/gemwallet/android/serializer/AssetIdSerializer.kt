package com.gemwallet.android.serializer

import com.gemwallet.android.ext.findByString
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import java.lang.reflect.Type

class AssetIdSerializer : JsonDeserializer<AssetId?>, JsonSerializer<AssetId> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AssetId? {
        val jObj = json.asJsonObject
        val chainString = jObj["chain"].asString
        val chain = Chain.findByString(chainString) ?: return null
        val tokenId = if (jObj["tokenId"]?.isJsonNull != false) null else jObj["tokenId"].asString
        return AssetId(chain = chain, tokenId = tokenId)
    }

    override fun serialize(
        src: AssetId?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jObj = JsonObject()
        jObj.addProperty("chain", src?.chain?.string)
        jObj.addProperty("tokenId", src?.tokenId)
        return jObj
    }

}