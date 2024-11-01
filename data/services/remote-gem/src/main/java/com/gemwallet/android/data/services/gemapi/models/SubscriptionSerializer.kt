package com.gemwallet.android.data.services.gemapi.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Subscription
import java.lang.reflect.Type

class SubscriptionSerializer : JsonDeserializer<Subscription>, JsonSerializer<Subscription> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Subscription {
        val jObj = json.asJsonObject ?: throw JsonSyntaxException(json.toString())
        val chainString = jObj["chain"]?.asString ?: throw JsonSyntaxException(json.toString())
        val chain = Chain.entries.firstOrNull {
            chainString == it.string
        } ?: throw JsonSyntaxException(json.toString())
        return Subscription(
            address = jObj["address"]?.asString ?: throw JsonSyntaxException(json.toString()),
            chain = chain,
            wallet_index = jObj["wallet_index"]?.asInt ?: throw JsonSyntaxException(json.toString()),
        )
    }

    override fun serialize(
        src: Subscription,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonObject().apply {
            addProperty("address", src.address)
            addProperty("chain", src.chain.string)
            addProperty("wallet_index", src.wallet_index)
        }
    }
}