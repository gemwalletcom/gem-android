package com.gemwallet.android.data.services.gemapi.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.SwapApprovalData
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.SwapQuote
import com.wallet.core.primitives.SwapQuoteData
import java.lang.reflect.Type

class SwapQuoteDeserializer : JsonDeserializer<SwapQuote> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SwapQuote {
        val jObj = json?.asJsonObject ?: throw IllegalArgumentException()
        val data = if (jObj["data"].isJsonNull) {
            null
        } else if (jObj["data"].isJsonObject) {
            val jData = jObj["data"].asJsonObject
            SwapQuoteData(
                to = jData["to"].asString,
                value = jData["value"].asString,
                data = jData["data"].asString,
            )
        } else {
            null
        }
        val approval = if (jObj["approval"] == null || jObj["approval"].isJsonNull
            || !jObj["approval"].isJsonObject
            || jObj["approval"].asJsonObject["spender"].isJsonNull
            || !jObj["approval"].asJsonObject["spender"].isJsonPrimitive) {
            null
        } else {
            SwapApprovalData(spender = jObj["approval"].asJsonObject["spender"].asString)
        }
        return SwapQuote(
            chainType = ChainType.entries.firstOrNull { it.string == jObj.get("chainType").asString } ?: throw JsonSyntaxException("Unsupported chain"),
            fromAmount = jObj["fromAmount"].asString,
            toAmount = jObj["toAmount"].asString,
            feePercent = jObj["feePercent"].asFloat,
            provider = SwapProvider(
                name = jObj["provider"].asJsonObject.get("name").asString,
            ),
            data = data,
            approval = approval,
        )
    }
}