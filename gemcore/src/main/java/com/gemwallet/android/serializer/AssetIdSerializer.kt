package com.gemwallet.android.serializer

import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = AssetId::class)
object AssetIdSerializer {
    override fun serialize(encoder: Encoder, value: AssetId) = when (encoder) {
        is JsonEncoder -> encoder.encodeJsonElement(JsonPrimitive(value.toIdentifier()))
        else -> encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): AssetId = when (decoder) {
        is JsonDecoder -> decoder.decodeJsonElement().let {
            try {
                it.jsonPrimitive.content.toAssetId() ?: throw IOException("AssetId is null")
            } catch (_: IllegalArgumentException) {
                val chain = it.jsonObject.getValue("chain").jsonPrimitive.content.lowercase()
                val token = if (it.jsonObject.containsKey("tokenId")) {
                    it.jsonObject.getValue("tokenId").jsonPrimitive.jsonPrimitive.contentOrNull
                } else {
                    null
                }
                if (token.isNullOrEmpty()) {
                    chain.toAssetId()
                } else {
                    "${chain}_$token".toAssetId()
                } ?: throw IOException("AssetId is incorrect")
            }
        }
        else -> decoder.decodeString().toAssetId() ?: throw IOException("AssetId is null")
    }
}