package com.gemwallet.android.data.services.gemapi.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import java.lang.reflect.Type

class NodeSerializer : JsonDeserializer<Node>, JsonSerializer<Node> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Node {
        val jObj = json?.asJsonObject ?: throw IllegalArgumentException()
        val status = jObj["status"].asString.lowercase()
        return Node(
            url = jObj["url"].asString,
            priority = jObj["priority"].asInt,
            status = NodeState.entries.firstOrNull { it.string == status }
                ?: throw IllegalArgumentException()
        )
    }

    override fun serialize(
        src: Node,
        typeOfSrc: Type,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonObject().apply {
            addProperty("url", src.url)
            addProperty("priority", src.priority)
            addProperty("status", src.status.string)
        }
    }
}