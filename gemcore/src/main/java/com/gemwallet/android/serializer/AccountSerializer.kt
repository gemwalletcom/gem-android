package com.gemwallet.android.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import org.json.JSONException
import java.lang.reflect.Type

class AccountSerializer : JsonDeserializer<Account> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Account {
        val jObj = json.asJsonObject
        return Account(
            chain = Chain.entries.firstOrNull { it.name == jObj["chain"].asString }  ?: throw JSONException("Can't read account"),
            address = jObj["address"].asString,
            derivationPath = jObj["derivationPath"].asString,
            extendedPublicKey = jObj["extendedPublicKey"].asString
        )
    }
}