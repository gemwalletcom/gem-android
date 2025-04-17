package com.gemwallet.android.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = BigInteger::class)
object BigIntegerSerializer {
    override fun serialize(encoder: Encoder, value: BigInteger) = when (encoder) {
        is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(value.toString()))
        else -> encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigInteger = when (decoder) {
        is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content.toBigInteger()
        else -> decoder.decodeString().toBigInteger()
    }
}