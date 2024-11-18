package com.gemwallet.android.data.services.gemapi.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

class TransactionsSerializer : JsonDeserializer<Transactions> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Transactions {
        val jArr = json.asJsonArray ?: throw JsonSyntaxException(json.toString())
        val result = Transactions()
        jArr.mapNotNull {
            TransactionDeserialize.toTransaction(it)
        }.forEach {
            result.add(it)
        }
        return result
    }
}