package com.gemwallet.android.data.services.gemapi.models

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import com.gemwallet.android.data.services.gemapi.Transactions
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
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        format.timeZone = TimeZone.getTimeZone("GMT")
        val jArr = json.asJsonArray ?: throw JsonSyntaxException(json.toString())
        val result = Transactions()
        jArr.mapNotNull {
            TransactionDeserialize.toTransaction(it, format)
        }.forEach {
            result.add(it)
        }
        return result
    }
}